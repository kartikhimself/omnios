package com.quewelcy.omnios.service;

import android.app.Service;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.quewelcy.omnios.Configures.Actions;
import static com.quewelcy.omnios.Configures.DirFileComparator.NAME_SORT;
import static com.quewelcy.omnios.Configures.Extras;

public class OmniosService extends Service implements StreamAudioPlayer.StreamListener {

    private static final FileFilter MP3_FILTER = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isFile() && f.getName().toLowerCase().endsWith(Configures.DOT_MP3);
        }
    };
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
                    case Actions.PLAY_STATE:
                        playPause();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((OreoNotificationHelper) mNotificationHelper).showNotifier(mCurrentPlayable,
                    ((OreoSessionHelper) mSessionHelper).getOreoSessionToken());
        } else {
            mNotificationHelper.showNotifier(mCurrentPlayable, mSessionHelper.getSessionToken());
        }
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
                        if (intent.getIntExtra("state", 0) == 0) {
                            stopPlayback();
                        }
                        break;
                }
            }
        };

        final IntentFilter filter = new IntentFilter();
        filter.addCategory(Actions.CATEGORY_BROADCAST);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);

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
        if (mCurrentPlayable == null) {
            return;
        }
        PrefHelper.setAudioPosition(OmniosService.this, mCurrentPlayable.getPath(), mPlayer.getCurrentPosition());
        mCurrentPlayable = null;

        if (mPlayer == null) {
            return;
        }
        mPlayer.stop();
        mPlayer = null;

        setPlaybackState(PlayState.PAUSE, "");
        mNotificationHelper.dismissNotifier();
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

    public void seekLeft() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.seekMillis(mPlayer.getCurrentPosition() - 20000);
        }
    }

    public void seekRight() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.seekMillis(mPlayer.getCurrentPosition() + 20000);
        }
    }

    public void seekPercent(int progress) {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.seekPercent(progress);
        }
    }

    public void play(Playable playable) {
        if (playable != null &&
                mCurrentPlayable != null &&
                playable.equals(mCurrentPlayable)) {
            return;
        }
        stopPlayback();
        startPlayback(playable, true);
    }

    private void playNeighbour(Direction direction) {
        if (mCurrentPlayable == null) {
            return;
        }
        File nextFile = null;
        if (!queue.isEmpty()) {
            nextFile = new File(queue.poll());
        } else {
            File currentFile = new File(mCurrentPlayable.getPath());
            if (currentFile.exists()) {
                File[] files = currentFile.getParentFile().listFiles(MP3_FILTER);
                Arrays.sort(files, NAME_SORT);
                int position = -1;
                for (int i = 0; i < files.length; i++) {
                    if (files[i].equals(currentFile)) {
                        position = i;
                        break;
                    }
                }
                if (position >= 0) {
                    if (direction == Direction.NEXT) {
                        if (position < files.length - 1) {
                            position++;
                        } else {
                            position = 0;
                        }
                    } else if (direction == Direction.PREV) {
                        if (position > 0) {
                            position--;
                        } else {
                            position = files.length - 1;
                        }
                    }
                    nextFile = files[position];
                }
            }
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

    private enum Direction {
        PREV, NEXT
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