package com.quewelcy.omnios.service.session;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;

import com.quewelcy.omnios.service.OmniosService;

@TargetApi(Build.VERSION_CODES.O)
public class OreoSessionHelper extends SessionHelper {

    private MediaSession mSessionOreo;

    public OreoSessionHelper(Context context, ComponentName mediaButtonReceiver) {
        super(context, mediaButtonReceiver);
    }

    @Override
    public void release() {
        if (mSessionOreo != null) {
            mSessionOreo.release();
        }
    }

    public MediaSession.Token getOreoSessionToken() {
        return mSessionOreo.getSessionToken();
    }

    @Override
    public void setMetadata(String title, Bitmap splashBitmap) {
        MediaMetadata.Builder metaOreo = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, splashBitmap);
        mSessionOreo.setMetadata(metaOreo.build());
    }

    @Override
    public void setMetadata() {
        mSessionOreo.setMetadata(new MediaMetadata.Builder().build());
    }

    @Override
    public void setPlaybackState(OmniosService.PlayState playState) {
        PlaybackState.Builder stateBuilderOreo = new PlaybackState.Builder();
        stateBuilderOreo.setActions(
                PlaybackState.ACTION_PLAY_PAUSE
                        | PlaybackState.ACTION_SKIP_TO_NEXT
                        | PlaybackState.ACTION_SKIP_TO_PREVIOUS);
        stateBuilderOreo.setState(playState == OmniosService.PlayState.PLAY ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED, 10L, 1F);
        mSessionOreo.setPlaybackState(stateBuilderOreo.build());
    }

    @Override
    public void registerMedia() {
        mSessionOreo = new MediaSession(
                mContext,
                OMNIOS_SERVICE_TAG);
        mSessionOreo.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSessionOreo.setActive(true);
    }
}
