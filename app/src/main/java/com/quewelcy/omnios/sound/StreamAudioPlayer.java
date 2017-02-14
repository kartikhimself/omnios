package com.quewelcy.omnios.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.PowerManager;

import static com.quewelcy.omnios.Configures.millisToTimeString;

public class StreamAudioPlayer implements OnCompletionListener, OnPreparedListener, OnErrorListener {

    private MediaPlayer mMediaPlayer;
    private StreamListener mListener;
    private Context mContext;
    private boolean mIsCancelled;
    private int mPosition;

    public StreamAudioPlayer(StreamListener listener, Context context) {
        this.mListener = listener;
        mContext = context;
        mIsCancelled = false;
        mPosition = 0;
    }

    public void playUrl(String link, int position) {
        mPosition = position;
        playUrl(link);
    }

    public void playUrl(String link) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);

        try {
            mMediaPlayer.setDataSource(link);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            //nothing
        }
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        if (mListener != null) {
            mListener.onPlaybackEnd();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mListener != null) {
            mListener.onPlaybackError();
        }
        stop();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaplayer) {
        if (mIsCancelled) {
            return;
        }
        if (mPosition > 0) {
            mMediaPlayer.seekTo(mPosition);
        }
        mMediaPlayer.start();
        if (mListener != null) {
            mListener.onPlaybackStart();
        }
    }

    public boolean isPlaying() {
        try {
            return mMediaPlayer != null
                    && mMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public void stop() {
        mIsCancelled = true;
        mListener = null;
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (Exception e) {
                //nothing
            }
        }
    }

    public void seekPercent(int progress) {
        if (mMediaPlayer != null
                && mMediaPlayer.getDuration() > 0) {
            mMediaPlayer.seekTo(progress * mMediaPlayer.getDuration() / 100);
        }
    }

    public void seekMillis(int millis) {
        if (mMediaPlayer != null
                && mMediaPlayer.getDuration() > 0
                && millis < mMediaPlayer.getDuration()) {
            mMediaPlayer.seekTo(millis);
        }
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public String getTimeCurrent() {
        return millisToTimeString(mMediaPlayer.getCurrentPosition());
    }

    public String getTimeEnd() {
        return millisToTimeString(mMediaPlayer.getDuration());
    }

    public int getProgress() {
        return 100 * mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration();
    }

    public void lowerVolume() {
        mMediaPlayer.setVolume(0.1f, 0.1f);
    }

    public void revertVolume() {
        mMediaPlayer.setVolume(1.0f, 1.0f);
    }

    public interface StreamListener {
        void onPlaybackError();

        void onPlaybackStart();

        void onPlaybackEnd();
    }
}