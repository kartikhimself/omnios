package com.quewelcy.omnios.service.session;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.quewelcy.omnios.service.OmniosService;

public class SessionHelper {

    static final String OMNIOS_SERVICE_TAG = "OMNIOS_SERVICE_TAG";
    final Context mContext;
    private final ComponentName mMediaButtonReceiver;
    private MediaSessionCompat mSession;

    public SessionHelper(Context context, ComponentName mediaButtonReceiver) {
        mContext = context;
        mMediaButtonReceiver = mediaButtonReceiver;
    }

    public void release() {
        if (mSession != null) {
            mSession.release();
        }
    }

    public MediaSessionCompat.Token getSessionToken() {
        return mSession.getSessionToken();
    }

    public void setMetadata(String title, Bitmap splashBitmap) {
        MediaMetadataCompat.Builder metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, splashBitmap);
        mSession.setMetadata(metadata.build());
    }

    public void setMetadata() {
        mSession.setMetadata(new MediaMetadataCompat.Builder().build());
    }

    public void setPlaybackState(OmniosService.PlayState playState) {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        stateBuilder.setState(playState == OmniosService.PlayState.PLAY ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, 10L, 1F);
        mSession.setPlaybackState(stateBuilder.build());
    }

    public void registerMedia() {
        mSession = new MediaSessionCompat(
                mContext,
                OMNIOS_SERVICE_TAG,
                mMediaButtonReceiver, null);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);
    }
}