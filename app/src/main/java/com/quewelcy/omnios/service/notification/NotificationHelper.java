package com.quewelcy.omnios.service.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.quewelcy.omnios.Configures;
import com.quewelcy.omnios.OmniosActivity;
import com.quewelcy.omnios.R;
import com.quewelcy.omnios.data.Playable;
import com.quewelcy.omnios.service.OmniosService;
import com.quewelcy.omnios.view.pattern.CrossRopes;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

public class NotificationHelper extends ContextWrapper {

    static final int NOTIFIER_ID = 1;
    static final String CHANNEL_ONE_ID = "com.quewelcy.omnios.media";

    final Service mService;
    final NotificationManager mNotificationManager;

    public NotificationHelper(Service service) {
        super(service);
        mService = service;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void dismissNotifier() {
        mService.stopForeground(true);
        mNotificationManager.cancelAll();
    }

    public void showNotifier(Playable playable, MediaSessionCompat.Token token, boolean setPlay) {
        if (playable == null) {
            return;
        }

        try {
            Intent intentSeekLeft = new Intent(getApplicationContext(), OmniosService.class);
            intentSeekLeft.setAction(Configures.Actions.SEEK_LEFT);

            Intent intentPrev = new Intent(getApplicationContext(), OmniosService.class);
            intentPrev.setAction(Configures.Actions.PREV);

            Intent intentPp = new Intent(this, OmniosService.class);
            intentPp.setAction(Configures.Actions.PLAY_PAUSE);
            intentPp.putExtra(Configures.Extras.DISMISS_NOTIFIER, !setPlay);

            Intent intentNext = new Intent(this, OmniosService.class);
            intentNext.setAction(Configures.Actions.NEXT);

            Intent intentSeekRight = new Intent(getApplicationContext(), OmniosService.class);
            intentSeekRight.setAction(Configures.Actions.SEEK_RIGHT);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ONE_ID)
                    .setContentText(playable.getTitle())
                    .setSmallIcon(R.drawable.ic_launcher_white)
                    .setAutoCancel(false)
                    .setLargeIcon(new CrossRopes(300).getBitmap())
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, OmniosActivity.class), FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_rew,
                            getString(R.string.notification_seek_left),
                            PendingIntent.getService(getApplicationContext(), 0, intentSeekLeft, FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_previous,
                            getString(R.string.notification_prev),
                            PendingIntent.getService(getApplicationContext(), 0, intentPrev, FLAG_CANCEL_CURRENT))
                    .addAction(setPlay ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause,
                            getString(setPlay ? R.string.notification_play : R.string.notification_stop),
                            PendingIntent.getService(getApplicationContext(), 0, intentPp, FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_next,
                            getString(R.string.notification_next),
                            PendingIntent.getService(getApplicationContext(), 0, intentNext, FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_ff,
                            getString(R.string.notification_seek_right),
                            PendingIntent.getService(getApplicationContext(), 0, intentSeekRight, FLAG_CANCEL_CURRENT))
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(token)
                            .setShowActionsInCompactView(0, 2, 4))
                    .build();
            if (setPlay) {
                notification.flags &= ~Notification.FLAG_NO_CLEAR;
            } else {
                notification.flags |= Notification.FLAG_NO_CLEAR;
            }
            mService.startForeground(NOTIFIER_ID, notification);
        } catch (Exception e) {
            Log.e("audio_error", "can't show notifier", e);
        }
    }
}