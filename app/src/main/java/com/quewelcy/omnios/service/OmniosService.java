package com.quewelcy.omnios.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.quewelcy.omnios.Configures;
import com.quewelcy.omnios.Configures.Direction;
import com.quewelcy.omnios.data.Playable;
import com.quewelcy.omnios.data.PrefHelper;
import com.quewelcy.omnios.receiver.MusicIntentReceiver;
import com.quewelcy.omnios.service.notification.NotificationHelper;
import com.quewelcy.omnios.service.notification.OreoNotificationHelper;
import com.quewelcy.omnios.service.session.OreoSessionHelper;
import com.quewelcy.omnios.service.session.SessionHelper;
import com.quewelcy.omnios.sound.StreamAudioPlayer;
import com.quewelcy.omnios.view.pattern.Mosaic;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.quewelcy.omnios.Configures.Actions;
import static com.quewelcy.omnios.Configures.Extras;
import static com.quewelcy.omnios.Configures.Extras.STATE;

public class OmniosService extends Service implements StreamAudioPlayer.StreamListener {

    private static final FileFilter MP3_FILTER = f -> f.isFile() && f.getName().toLowerCase().endsWith(Configures.DOT_MP3);
    private final IBinder mBinder = new OmniosBinder();
    private final Bundle mBundle = new Bundle();
    private final Queue<String> queue = new LinkedList<>();

    private PhoneStateListener mPhoneStateListener;
    private BroadcastReceiver mReceiver;
    private StreamAudioPlayer mPlayer;
    private TelephonyManager mTelephonyManager;
    private AudioManager mAudioManager;
    private Playable mCurrentPlayable;
    private NotificationHelper mNotificationHelper;
    private SessionHelper mSessionHelper;

    public Bundle getCurrentTime() {
        mBundle.clear();
        if (mPlayer != null
                && mPlayer.isPlaying()
                && mCurrentPlayable != null) {
            mBundle.putString(Extras.TIME_CUR, mPlayer.getTimeCurrent());
            mBundle.putString(Extras.TIME_END, mPlayer.getTimeEnd());
            mBundle.putInt(Extras.PROGRESS, mPlayer.getProgress());
        }
        return mBundle;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mCurrentPlayable = null;
        registerBroadcastReceiver();
        registerPhoneStateReceiver();

        ComponentName mediaButtonReceiver = new ComponentName(this, MusicIntentReceiver.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationHelper = new OreoNotificationHelper(this);
            mSessionHelper = new OreoSessionHelper(this, mediaButtonReceiver);
        } else {
            mNotificationHelper = new NotificationHelper(this);
            mSessionHelper = new SessionHelper(this, mediaButtonReceiver);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case Actions.PREV:
                        stopPlayback();
                        validateCurrentPlayable();
                        playNeighbour(Direction.PREV);
                        break;
                    case Actions.NEXT:
                        stopPlayback();
                        validateCurrentPlayable();
                        playNeighbour(Direction.NEXT);
                        break;
                    case Actions.PLAY_PAUSE:
                        playPause();
                        Bundle extras = intent.getExtras();
                        boolean isDismissed = false;
                        if (extras != null) {
                            Object dismissNotifier = extras.get(Extras.DISMISS_NOTIFIER);
                            if (dismissNotifier instanceof Boolean && (boolean) dismissNotifier) {
                                dismissNotifier();
                                isDismissed = true;
                            }
                        }
                        if (!isDismissed) {
                            setPlayNotifier(true);
                        }
                        break;
                    case Actions.SEEK_LEFT:
                        seekLeft(60);
                        break;
                    case Actions.SEEK_RIGHT:
                        seekRight(60);
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayback();
        abandonFocus();
        unregisterReceiver(mReceiver);
        unregisterPhoneStateReceiver();
        mSessionHelper.release();
    }

    @Override
    public void onPlaybackError() {
        Intent intent = new Intent(Actions.ERROR_PLAYING);
        intent.addCategory(Actions.CATEGORY_BROADCAST);
        sendBroadcast(intent);
        stopPlayback();
    }

    @Override
    public void onPlaybackStart() {
        setPlayNotifier(false);
    }

    @Override
    public void onPlaybackEnd() {
        playNeighbour(Direction.NEXT);
    }

    private void registerPhoneStateReceiver() {
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (mPlayer != null) {
                        mPlayer.lowerVolume();
                    }
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    if (mPlayer != null) {
                        mPlayer.revertVolume();
                    }
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void unregisterPhoneStateReceiver() {
        if (mTelephonyManager != null && mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private void registerBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (intent == null || intent.getAction() == null) {
                    return;
                }
                switch (intent.getAction()) {
                    case Intent.ACTION_HEADSET_PLUG:
                        if (intent.getIntExtra(STATE, 0) == 0) {
                            stopPlayback();
                            setPlayNotifier(true);
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        stopPlayback();
                        setPlayNotifier(true);
                        break;
                }
            }
        };

        final IntentFilter filter = new IntentFilter();
        filter.addCategory(Actions.CATEGORY_BROADCAST);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(mReceiver, filter);
    }

    private void startPlayback(Playable playable, boolean payAttentionToPosition) {
        if (playable == null) {
            return;
        }
        PrefHelper.setCurrentPlayable(OmniosService.this, playable);
        mCurrentPlayable = playable;
        registerMedia(playable.getTitle());
        mPlayer = new StreamAudioPlayer(this, getApplicationContext());
        if (payAttentionToPosition) {
            long position = PrefHelper.getAudioPosition(OmniosService.this, playable.getPath());
            mPlayer.playUrl(mCurrentPlayable.getPath(), (int) position);
        } else {
            mPlayer.playUrl(mCurrentPlayable.getPath());
        }
        sendInvalidateEvent();
    }

    public void stopPlayback() {
        mSessionHelper.release();
        if (mCurrentPlayable == null || mPlayer == null) {
            return;
        }
        PrefHelper.setAudioPosition(OmniosService.this, mCurrentPlayable.getPath(), mPlayer.getCurrentPosition());

        mPlayer.stop();
        mPlayer = null;

        setPlaybackState(PlayState.PAUSE, "");
    }

    private void setPlaybackState(PlayState playState, String title) {
        if (title != null && !title.isEmpty()) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            Bitmap splashBitmap = new Mosaic(dm.widthPixels / 2, dm.heightPixels / 2).getBitmap();
            mSessionHelper.setMetadata(title, splashBitmap);
        } else {
            mSessionHelper.setMetadata();
        }
        mSessionHelper.setPlaybackState(playState);
    }

    private void registerMedia(String title) {
        requestFocus();
        mSessionHelper.registerMedia();
        setPlaybackState(PlayState.PLAY, title);
    }

    private void requestFocus() {
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void abandonFocus() {
        mAudioManager.abandonAudioFocus(null);
    }

    private void sendInvalidateEvent() {
        Intent intent = new Intent(Actions.INVALIDATE);
        intent.addCategory(Actions.CATEGORY_BROADCAST);
        sendBroadcast(intent);
    }

    private void validateCurrentPlayable() {
        if (mCurrentPlayable == null) {
            mCurrentPlayable = PrefHelper.getCurrentPlayable(this);
        }
    }

    private void playPause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            stopPlayback();
        } else {
            validateCurrentPlayable();
            startPlayback(mCurrentPlayable, true);
        }
    }

    public void setPlayNotifier(boolean setPlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((OreoNotificationHelper) mNotificationHelper).showNotifier(mCurrentPlayable,
                    ((OreoSessionHelper) mSessionHelper).getOreoSessionToken(), setPlay);
        } else {
            mNotificationHelper.showNotifier(mCurrentPlayable, mSessionHelper.getSessionToken(), setPlay);
        }
        if (setPlay) {
            stopForeground(false);
        }
    }

    public void dismissNotifier() {
        mNotificationHelper.dismissNotifier();
    }

    public void seekLeft(int sec) {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.seekMillis(mPlayer.getCurrentPosition() - sec * 1000);
        }
    }

    public void seekRight(int sec) {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.seekMillis(mPlayer.getCurrentPosition() + sec * 1000);
        }
    }

    public void seekPercent(int progress) {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.seekPercent(progress);
        }
    }

    public void play(Playable playable) {
        if (mPlayer != null && mPlayer.isPlaying() && playable != null && playable.equals(mCurrentPlayable)) {
            return;
        }
        stopPlayback();
        startPlayback(playable, true);
    }

    private void playNeighbour(Direction direction) {
        if (mCurrentPlayable == null) {
            return;
        }
        File nextFile;
        if (!queue.isEmpty()) {
            nextFile = new File(queue.poll());
        } else {
            nextFile = Configures.getNeighbour(direction, new File(mCurrentPlayable.getPath()), MP3_FILTER);
        }
        if (nextFile == null) {
            return;
        }
        //check if current playable was booked
        Playable newPlayable = new Playable(Configures.dropExtension(nextFile.getName()), nextFile.getAbsolutePath(), 0);
        if (mCurrentPlayable != null) {
            PrefHelper.checkAndContinueAudioPosition(OmniosService.this, mCurrentPlayable, newPlayable);
        }
        startPlayback(newPlayable, false);
    }

    public void addToQueue(String path) {
        queue.offer(path);
    }

    public List<Playable> getQueueCopy() {
        List<Playable> queued = new ArrayList<>();
        for (String path : queue) {
            queued.add(new Playable(Configures.getTitle(path), path, 0));
        }
        return queued;
    }

    public void clearQueue() {
        queue.clear();
    }

    public void removeFromQueue(String path) {
        queue.remove(path);
    }

    public enum PlayState {
        PLAY, PAUSE
    }

    public class OmniosBinder extends Binder {
        public OmniosService getService() {
            return OmniosService.this;
        }
    }
}