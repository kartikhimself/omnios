package com.quewelcy.omnios.service.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Build;
import android.util.Log;

import com.quewelcy.omnios.Configures;
import com.quewelcy.omnios.OmniosActivity;
import com.quewelcy.omnios.R;
import com.quewelcy.omnios.data.Playable;
import com.quewelcy.omnios.service.OmniosService;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

@TargetApi(Build.VERSION_CODES.O)
public class OreoNotificationHelper extends NotificationHelper {

    private static final String CHANNEL_ONE_NAME = "Media Channel";

    public OreoNotificationHelper(Service context) {
        super(context);
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(notificationChannel);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void showNotifier(Playable playable, MediaSession.Token token) {
        if (playable == null) {
            return;
        }

        try {
            Intent intentPrev = new Intent(getApplicationContext(), OmniosService.class);
            intentPrev.setAction(Configures.Actions.PREV);

            Intent intentPp = new Intent(this, OmniosService.class);
            intentPp.setAction(Configures.Actions.PLAY_PAUSE);

            Intent intentNext = new Intent(this, OmniosService.class);
            intentNext.setAction(Configures.Actions.NEXT);

            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                    .setContentText(playable.getTitle())
                    .setColorized(true)
                    .setColor(getColor(R.color.primary))
                    .setSmallIcon(R.drawable.ic_launcher_white)
                    .setAutoCancel(false)
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), OmniosActivity.class), FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_previous, getString(R.string.notification_prev), PendingIntent.getService(getApplicationContext(), 0, intentPrev, FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_pause, getString(R.string.notification_stop), PendingIntent.getService(getApplicationContext(), 0, intentPp, FLAG_CANCEL_CURRENT))
                    .addAction(android.R.drawable.ic_media_next, getString(R.string.notification_next), PendingIntent.getService(getApplicationContext(), 0, intentNext, FLAG_CANCEL_CURRENT))
                    .setStyle(new Notification.MediaStyle()
                            .setMediaSession(token)
                            .setShowActionsInCompactView(0, 1, 2))
                    .build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            mService.startForeground(NOTIFIER_ID, notification);
        } catch (Exception e) {
            Log.e("audio_error", "can't show notifier", e);
        }
    }
}