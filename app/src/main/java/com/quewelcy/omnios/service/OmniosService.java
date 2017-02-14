package com.quewelcy.omnios.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.quewelcy.omnios.Configures;
import com.quewelcy.omnios.OmniosActivity;
import com.quewelcy.omnios.R;
import com.quewelcy.omnios.data.Playable;
import com.quewelcy.omnios.data.PrefHelper;
import com.quewelcy.omnios.receiver.MusicIntentReceiver;
import com.quewelcy.omnios.sound.StreamAudioPlayer;
import com.quewelcy.omnios.view.pattern.CrossRopes;
import com.quewelcy.omnios.view.pattern.PaperStack;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static com.quewelcy.omnios.Configures.Actions;
import static com.quewelcy.omnios.Configures.DirFileComparator.NAME_SORT;
import static com.quewelcy.omnios.Configures.Extras;

public class OmniosService extends Service implements StreamAudioPlayer.StreamListener {

    private static final int NOTIFIER_ID = 1;
    private static final String OMNIOS_SERVICE_TAG = "OMNIOS_SERVICE_TAG";
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
    private MediaSessionCompat mSession;
    private BroadcastReceiver mReceiver;
    private StreamAudioPlayer mPlayer;
    private TelephonyManager mTelephonyManager;
    private ComponentName mMediaButtonReceiverComponent;
    private AudioManager mAudioManager;
    private Playable mCurrentPlayable;
    private NotificationManager mNotificationManager;

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
        mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mCurrentPlayable = null;
        registerBroadcastReceiver();
        registerPhoneStateReceiver();
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
        if (mSession != null) {
            mSession.release();
        }
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
        showNotifier();
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
                String action = intent.getAction();
                switch (action) {
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
        if (mSession != null) {
            mSession.release();
        }
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
        dismissNotifier();
    }

    private void setPlaybackState(PlayState playState, String title) {
        if (mSession == null) {
            return;
        }
        if (title != null && !title.isEmpty()) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            Bitmap splashBitmap = new PaperStack(dm.widthPixels / 2, dm.heightPixels / 2).getBitmap();
            MediaMetadataCompat.Builder metadata = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, splashBitmap);
            mSession.setMetadata(metadata.build());
        } else {
            mSession.setMetadata(new MediaMetadataCompat.Builder().build());
        }
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        stateBuilder.setState(playState == PlayState.PLAY ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, 10L, 1F);
        mSession.setPlaybackState(stateBuilder.build());
    }

    private void registerMedia(String title) {
        requestFocus();
        mSession = new MediaSessionCompat(
                OmniosService.this,
                OMNIOS_SERVICE_TAG,
                mMediaButtonReceiverComponent, null);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);
        setPlaybackState(PlayState.PLAY, title);
    }

    private void requestFocus() {
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void abandonFocus() {
        mAudioManager.abandonAudioFocus(null);
    }

    private void dismissNotifier() {
        stopForeground(true);
        mNotificationManager.cancelAll();
    }

    public void showNotifier() {
        dismissNotifier();

        if (mCurrentPlayable == null) {
            return;
        }

        try {
            Intent intentPrev = new Intent(getApplicationContext(), OmniosService.class);
            intentPrev.setAction(Actions.PREV);

            Intent intentPp = new Intent(this, OmniosService.class);
            intentPp.setAction(Actions.PLAY_STATE);

            Intent intentNext = new Intent(this, OmniosService.class);
            intentNext.setAction(Actions.NEXT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder.setContentTitle(mCurrentPlayable.getTitle())
                    .setSmallIcon(R.drawable.ic_launcher_white)
                    .setContentText(getString(R.string.app_name))
                    .setAutoCancel(false)
                    .setLargeIcon(new CrossRopes(300).getBitmap())
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, OmniosActivity.class), FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_previous, getString(R.string.notification_prev), PendingIntent.getService(getApplicationContext(), 0, intentPrev, FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_pause, getString(R.string.notification_stop), PendingIntent.getService(getApplicationContext(), 0, intentPp, FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_next, getString(R.string.notification_next), PendingIntent.getService(getApplicationContext(), 0, intentNext, FLAG_CANCEL_CURRENT))
                    .setStyle(new NotificationCompat.MediaStyle()
                            .setMediaSession(mSession.getSessionToken())
                            .setShowActionsInCompactView(0, 1, 2));

            Notification notification;
            notification = notificationBuilder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            startForeground(NOTIFIER_ID, notification);
        } catch (Exception e) {
            Log.e("audio_error", "can't show notifier", e);
        }
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

    private enum PlayState {
        PLAY, PAUSE
    }

    public class OmniosBinder extends Binder {
        public OmniosService getService() {
            return OmniosService.this;
        }
    }
}