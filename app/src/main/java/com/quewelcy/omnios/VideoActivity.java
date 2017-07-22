package com.quewelcy.omnios;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.quewelcy.omnios.Configures.Extras;
import com.quewelcy.omnios.Configures.PermissionRequestCode;
import com.quewelcy.omnios.data.Playable;
import com.quewelcy.omnios.data.PrefHelper;
import com.quewelcy.omnios.view.moving.MeanderBgSurfaceView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
import static com.quewelcy.omnios.Configures.DirFileComparator.NAME_SORT;
import static com.quewelcy.omnios.Configures.millisToTimeString;

public class VideoActivity extends AppCompatActivity
        implements IVLCVout.Callback {

    private static final FileFilter VIDEO_FILTER = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isFile() && Configures.isVideo(f.getName().toLowerCase());
        }
    };
    private PhoneStateListener mPhoneStateListener;
    private BroadcastReceiver mReceiver;
    private TelephonyManager mTelephonyManager;

    private MeanderBgSurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private ProgressBar mSpin;
    private View mSeekBox;
    private Toolbar mToolbar;
    private SeekBar mSeek;
    private ImageView mPauseButton;
    private TextView mEndTime;
    private TextView mCurTime;

    private int mVideoWidth;
    private int mVideoHeight;
    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;

    private String mUrl;
    private boolean mDragging;
    private boolean mIsPaused = false;
    private boolean mIsLocked = false;

    private Timer mSeekTimer;
    private TimerTask mSeekTask;

    private Set<String> requestedPermissions = new HashSet<>();

    private CountDownTimer mHideTimer = new CountDownTimer(3000, 3000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            hideControls();
        }
    };

    private MediaPlayer.EventListener mPlayerListener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    playNextIfPossible();
                    break;
                case MediaPlayer.Event.Vout:
                    mSpin.setVisibility(View.GONE);
                    mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                    mMediaPlayer.setTime(PrefHelper.getVideoPosition(VideoActivity.this, mUrl));
                    PrefHelper.setCurrentPlayable(VideoActivity.this, new Playable("", mUrl, 0));
                    break;
                default:
                    break;
            }
        }
    };
    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            updatePlayPauseState();
        }
    };

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            if (!fromUser || mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.setPosition(progress / 100F);
            if (mCurTime != null) {
                mCurTime.setText(millisToTimeString(mMediaPlayer.getTime()));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            updateProgress();
        }
    };

    private View.OnTouchListener mTripleTapListener = new View.OnTouchListener() {
        Handler handler = new Handler();
        int numberOfTaps = 0;
        long lastTapTimeMs = 0;
        long touchDownMs = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchDownMs = System.currentTimeMillis();
                    return false;
                case MotionEvent.ACTION_UP:
                    handler.removeCallbacksAndMessages(null);

                    if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                        numberOfTaps = 0;
                        lastTapTimeMs = 0;
                        break;
                    }

                    if (numberOfTaps > 0
                            && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
                        numberOfTaps += 1;
                    } else {
                        numberOfTaps = 1;
                    }
                    lastTapTimeMs = System.currentTimeMillis();

                    if (numberOfTaps == 3) {
                        toggleLock();
                        return true;
                    }
                    return false;
            }
            return false;
        }
    };

    private OnClickListener mControlClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsLocked) {
                return;
            }
            if (mToolbar.getVisibility() == View.VISIBLE) {
                hideControls();
            } else {
                showControls();
            }
        }
    };

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        supportRequestWindowFeature(AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR_OVERLAY);
        requestFullScreen();
        getWindow().setFlags(FLAG_HARDWARE_ACCELERATED, FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_video);

        mSpin = (ProgressBar) findViewById(R.id.activity_video_progress);
        mSpin.setVisibility(View.VISIBLE);

        mPauseButton = (ImageView) findViewById(R.id.activity_video_pause);
        mPauseButton.setOnClickListener(mPauseListener);

        mSeek = (SeekBar) findViewById(R.id.activity_video_seek);
        mSeek.setOnSeekBarChangeListener(mSeekListener);

        View controlBox = findViewById(R.id.activity_video_control_box);
        controlBox.setOnTouchListener(mTripleTapListener);
        controlBox.setOnClickListener(mControlClickListener);

        mSeekBox = findViewById(R.id.activity_video_seek_box);
        mCurTime = (TextView) findViewById(R.id.activity_video_cur_time);
        mEndTime = (TextView) findViewById(R.id.activity_video_end_time);
        mSurfaceView = (MeanderBgSurfaceView) findViewById(R.id.activity_video_surface);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mUrl = Configures.getRealPathFromURI(this, getIntent().getData());
        if (mUrl == null || mUrl.isEmpty()) {
            Toast.makeText(this, R.string.cant_find_location, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (mToolbar != null) {
                setSupportActionBar(mToolbar);
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setHomeButtonEnabled(true);
                    actionBar.setTitle(Configures.getTitle(mUrl));
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Set<String> granted = new HashSet<>();
        if (requestCode == PermissionRequestCode.REQ_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (grantResult == PERMISSION_GRANTED) {
                    switch (permission) {
                        case READ_PHONE_STATE:
                        case READ_EXTERNAL_STORAGE:
                            granted.add(permission);
                            break;
                    }
                }
            }
            if (!granted.isEmpty() && granted.size() == requestedPermissions.size()) {
                finish();
                startActivity(getIntent());
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (mIsLocked) {
            Toast.makeText(VideoActivity.this, R.string.unlock_to_go_back, Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        resizeVideo();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                requestedPermissions.add(READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) != PERMISSION_GRANTED) {
                requestedPermissions.add(READ_PHONE_STATE);
            }
            if (!requestedPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, requestedPermissions.toArray(new String[requestedPermissions.size()]), PermissionRequestCode.REQ_CODE);
            }
        }
        registerBroadcastReceiver();
        registerPhoneStateReceiver();
        mSeekTask = new TimerTask() {
            @Override
            public void run() {
                updateProgress();
            }
        };
        mSeekTimer = new Timer();
        mSeekTimer.scheduleAtFixedRate(mSeekTask, 1000, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSurfaceHolder = mSurfaceView.getHolder();
        if (mSurfaceHolder != null
                && (SDK_INT < VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)) {
            initVLC();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveCurrentPosition();
        mSeekTask.cancel();
        mSeekTimer.cancel();
        mHideTimer.cancel();
        unregisterReceiver(mReceiver);
        unregisterPhoneStateReceiver();
        releaseMediaPlayer();
    }

    private void initVLC() {
        try {
            mLibVLC = new LibVLC();
            mSurfaceHolder.setKeepScreenOn(true);

            mMediaPlayer = new MediaPlayer(mLibVLC);
            mMediaPlayer.setEventListener(mPlayerListener);

            final IVLCVout vOut = mMediaPlayer.getVLCVout();
            vOut.setVideoView(mSurfaceView);
            vOut.addCallback(this);
            vOut.attachViews();

            Media m = new Media(mLibVLC, mUrl);
            mMediaPlayer.setMedia(m);

            Log.d("va", "initVLC: " + mIsPaused);

            if (!mIsPaused) {
                mMediaPlayer.play();
                mHideTimer.start();
            } else {
                mSurfaceView.showSplash();
                requestFullScreen();
            }
            if (getIntent().getBooleanExtra(Extras.LOCK_ON_START, false)) {
                toggleLock();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    private void requestFullScreen() {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        } else {
            int flag = View.SYSTEM_UI_FLAG_FULLSCREEN;
            View decorView = getWindow().getDecorView();
            if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            decorView.setSystemUiVisibility(flag);
        }
    }

    private void toggleLock() {
        mIsLocked = !mIsLocked;
        if (mIsLocked) {
            hideControls();
            Toast.makeText(VideoActivity.this, R.string.video_locked, Toast.LENGTH_SHORT).show();
        } else {
            showControls();
        }
    }

    private void resizeVideo() {
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            return;
        }
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float ratioF = (float) dm.widthPixels / dm.heightPixels;
        float ratioV = (float) mVideoWidth / mVideoHeight;
        boolean isLandscape = dm.widthPixels > dm.heightPixels;
        int aWidth, aHeight;
        if (isLandscape) {
            if (ratioF > ratioV) {
                aWidth = (int) (dm.heightPixels * ratioV);
                aHeight = dm.heightPixels;
            } else {
                aWidth = dm.widthPixels;
                aHeight = (int) (dm.widthPixels / ratioV);
            }
        } else {
            if (ratioF < ratioV) {
                aWidth = dm.widthPixels;
                aHeight = (int) (dm.widthPixels / ratioV);
            } else {
                aWidth = (int) (dm.heightPixels * ratioV);
                aHeight = dm.heightPixels;
            }
        }
        mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

        LayoutParams lp = mSurfaceView.getLayoutParams();
        lp.width = aWidth;
        lp.height = aHeight;
        mSurfaceView.setLayoutParams(lp);
        mSurfaceView.invalidate();
    }

    private void playNextIfPossible() {
        File currentFile = new File(mUrl);
        if (currentFile.exists()) {
            File[] files = currentFile.getParentFile().listFiles(VIDEO_FILTER);
            Arrays.sort(files, NAME_SORT);
            int position = -1;
            for (int i = 0; i < files.length; i++) {
                if (files[i].equals(currentFile)) {
                    position = i;
                    break;
                }
            }
            if (position >= 0) {
                if (position < files.length - 1) {
                    position++;
                } else {
                    position = 0;
                }
                File nextFile = files[position];
                finish();
                if (!nextFile.equals(currentFile)) {
                    Intent intent = new Intent(VideoActivity.this, VideoActivity.class);
                    intent.setData(Uri.fromFile(nextFile));
                    intent.putExtra(Extras.LOCK_ON_START, mIsLocked);
                    startActivity(intent);
                }
            }
        }
    }

    private void registerBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction()) &&
                        intent.getIntExtra("state", 0) == 0 &&
                        mMediaPlayer != null) {
                    mMediaPlayer.pause();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Configures.Actions.CATEGORY_BROADCAST);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);

        registerReceiver(mReceiver, filter);
    }

    private void registerPhoneStateReceiver() {
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.pause();
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

    private void saveCurrentPosition() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.getPlayerState() == Media.State.Ended) {
            PrefHelper.setVideoPosition(this, mUrl, 0);
        } else {
            PrefHelper.setVideoPosition(this, mUrl, mMediaPlayer.getTime());
        }
    }

    private void releaseMediaPlayer() {
        if (mLibVLC == null) {
            return;
        }
        mMediaPlayer.stop();
        IVLCVout vOut = mMediaPlayer.getVLCVout();
        vOut.removeCallback(this);
        vOut.detachViews();
        mSurfaceHolder = null;
        mLibVLC.release();
        mLibVLC = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private void showControls() {
        updatePlayPauseState();
        mToolbar.post(new Runnable() {
            @Override
            public void run() {
                mPauseButton.setVisibility(View.VISIBLE);
                mToolbar.setVisibility(View.VISIBLE);
                mSeekBox.setVisibility(View.VISIBLE);
                mHideTimer.start();
            }
        });
    }

    private void hideControls() {
        mPauseButton.setVisibility(View.INVISIBLE);
        mToolbar.setVisibility(View.INVISIBLE);
        mSeekBox.setVisibility(View.INVISIBLE);
    }

    private void updatePlayPauseState() {
        if (mMediaPlayer == null) {
            return;
        }
        mPauseButton.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer.isPlaying()) {
                    mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    mPauseButton.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        });
    }

    private void updateProgress() {
        if (mMediaPlayer == null || !mMediaPlayer.isPlaying() || mDragging || mSeek == null) {
            return;
        }
        VideoActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mSeek.setProgress((int) (100 * mMediaPlayer.getPosition()));
                mCurTime.setText(millisToTimeString(mMediaPlayer.getTime()));
                mEndTime.setText(millisToTimeString(mMediaPlayer.getLength()));
            }
        });
    }

    private void doPauseResume() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mIsPaused = true;
        } else {
            mSurfaceView.clearSplash();
            mMediaPlayer.play();
            mIsPaused = false;
        }
    }

    @Override
    public void onNewLayout(IVLCVout vlcVOut, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        resizeVideo();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVOut) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVOut) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVOut) {

    }
}