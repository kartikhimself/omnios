package com.quewelcy.omnios.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    context.startService(new Intent(Configures.Actions.PLAY_STATE));
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    context.startService(new Intent(Configures.Actions.PLAY_STATE));
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    context.startService(new Intent(Configures.Actions.PLAY_STATE));
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    context.startService(new Intent(Configures.Actions.PLAY_STATE));
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    context.startService(new Intent(Configures.Actions.NEXT));
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    context.startService(new Intent(Configures.Actions.PREV));
                    break;
            }
        }
    }
}