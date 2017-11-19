package com.quewelcy.omnios.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.KeyEvent;

import com.quewelcy.omnios.Configures;

public class MusicIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null || intent.getExtras() == null) {
            return;
        }
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                return;
            }
            String action = null;
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    action = Configures.Actions.PLAY_STATE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    action = Configures.Actions.PLAY_STATE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    action = Configures.Actions.PLAY_STATE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    action = Configures.Actions.PLAY_STATE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    action = Configures.Actions.NEXT;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    action = Configures.Actions.PREV;
                    break;
            }
            if (action == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(action));
            } else {
                context.startService(new Intent(action));
            }
        }
    }
}