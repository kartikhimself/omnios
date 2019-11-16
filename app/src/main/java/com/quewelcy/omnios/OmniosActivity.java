package com.quewelcy.omnios;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.quewelcy.omnios.Configures.PermissionRequestCode;
import com.quewelcy.omnios.adapters.ControlAdapter;
import com.quewelcy.omnios.data.Playable;
import com.quewelcy.omnios.data.PrefHelper;
import com.quewelcy.omnios.fragments.FolderFragment;
import com.quewelcy.omnios.fragments.QueueFragment;
import com.quewelcy.omnios.fragments.RecyclerItemClickListener;
import com.quewelcy.omnios.fragments.SavedPathsFragment;
import com.quewelcy.omnios.fragments.ServiceCommunicator;
import com.quewelcy.omnios.service.OmniosService;
import com.quewelcy.omnios.view.mono.BookWhiteIcon;
import com.quewelcy.omnios.view.mono.ClearWhiteIcon;
import com.quewelcy.omnios.view.mono.PauseWhiteIcon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Handler.Callback;
import static android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
import static com.quewelcy.omnios.Configures.Actions;
import static com.quewelcy.omnios.Configures.Extras;
import static com.quewelcy.omnios.adapters.ControlAdapter.HOME;
import static com.quewelcy.omnios.adapters.ControlAdapter.SEEK_LEFT_20;
import static com.quewelcy.omnios.adapters.ControlAdapter.SEEK_LEFT_60;
import static com.quewelcy.omnios.adapters.ControlAdapter.SEEK_RIGHT_20;
import static com.quewelcy.omnios.adapters.ControlAdapter.SEEK_RIGHT_60;
import static com.quewelcy.omnios.adapters.ControlAdapter.SHOW_QUEUE;
import static com.quewelcy.omnios.adapters.ControlAdapter.SHOW_SAVED;
import static com.quewelcy.omnios.fragments.RecyclerItemClickListener.OnItemClickListener;

public class OmniosActivity extends AppCompatActivity implements ServiceCommunicator {

    private ActionBarDrawerToggle mDrawerToggle;
    private FolderFragment mFolderFragment;
    private SavedPathsFragment mSavedPathsFragment;
    private QueueFragment mQueueFragment;
    private BroadcastReceiver mReceiver;
    private DrawerLayout mDrawerLayout;
    private Timer mTimer;
    private ControlAdapter mControlAdapter;
    private OmniosService mService;
    private MenuItem mMenuRemember;
    private MenuItem mMenuClear;

    private final Set<String> mRequestedPermissions = new HashSet<>();

    private final OnMenuItemClickListener mMenuListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_stop:
                    stop();
                    mFolderFragment.resetCurrent();
                    break;
                case R.id.menu_continue:
                    PrefHelper.toggleCurrentPathToPerms(OmniosActivity.this);
                    mFolderFragment.updateData();
                    break;
                case R.id.menu_clear:
                    if (mQueueFragment.isVisible()) {
                        if (mService != null) {
                            mService.clearQueue();
                        }
                        mQueueFragment.updateData();
                    }
                    if (mSavedPathsFragment.isVisible()) {
                        PrefHelper.clearPerms(OmniosActivity.this);
                        mSavedPathsFragment.updateData();
                        mFolderFragment.updateData();
                    }
                    break;
            }
            return true;
        }
    };

    private final OnItemClickListener mControlClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if (position < 0) {
                return;
            }
            switch (mControlAdapter.getAction(position)) {
                case HOME:
                    mDrawerLayout.closeDrawers();
                    mFolderFragment.goToHomePath();
                    break;
                case SHOW_SAVED:
                    mDrawerLayout.closeDrawers();
                    showSavedPaths();
                    break;
                case SHOW_QUEUE:
                    mDrawerLayout.closeDrawers();
                    showQueue();
                    break;
                case SEEK_LEFT_20:
                    seekLeft(20);
                    break;
                case SEEK_RIGHT_20:
                    seekRight(20);
                    break;
                case SEEK_LEFT_60:
                    seekLeft(60);
                    break;
                case SEEK_RIGHT_60:
                    seekRight(60);
                    break;
            }
        }

        @Override
        public void onItemLongClick(int position) {

        }
    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            OmniosService.OmniosBinder binder = (OmniosService.OmniosBinder) service;
            mService = binder.getService();
            checkToPlay(getIntent());
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    private final Handler mHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mFolderFragment.setTime(
                    msg.getData().getString(Extras.TIME_CUR, "0:00"),
                    msg.getData().getString(Extras.TIME_END, "0:00"),
                    msg.getData().getInt(Extras.PROGRESS, 0));
            return true;
        }
    });

    private void showSavedPaths() {
        mSavedPathsFragment.updateData();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("To continue");
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mFolderFragment);
        ft.hide(mQueueFragment);
        ft.show(mSavedPathsFragment);
        ft.addToBackStack(null);
        ft.commit();

        mMenuClear.setVisible(true);
        mMenuRemember.setVisible(false);
    }

    private void showQueue() {
        mQueueFragment.updateData();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Queue");
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mFolderFragment);
        ft.hide(mSavedPathsFragment);
        ft.show(mQueueFragment);
        ft.addToBackStack(null);
        ft.commit();

        mMenuClear.setVisible(true);
        mMenuRemember.setVisible(false);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setFlags(FLAG_HARDWARE_ACCELERATED, FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_omnios);

        mDrawerLayout = findViewById(R.id.activity_omnios_drawer_layout);
        FragmentManager fragmentManager = getSupportFragmentManager();

        mSavedPathsFragment = (SavedPathsFragment) fragmentManager.findFragmentById(R.id.activity_omnios_saved);
        mQueueFragment = (QueueFragment) fragmentManager.findFragmentById(R.id.activity_omnios_queue);
        if (mQueueFragment != null) {
            mQueueFragment.setServiceCommunicator(this);
        }
        mFolderFragment = (FolderFragment) fragmentManager.findFragmentById(R.id.activity_omnios_recycler_folder);
        if (mFolderFragment != null) {
            mFolderFragment.setServiceCommunicator(this);
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.hide(mSavedPathsFragment);
        ft.hide(mQueueFragment);
        ft.show(mFolderFragment);
        ft.commit();

        mControlAdapter = new ControlAdapter(this);

        RecyclerView controlDrawer = findViewById(R.id.control_drawer);
        controlDrawer.setHasFixedSize(true);
        controlDrawer.addOnItemTouchListener(new RecyclerItemClickListener(this, mControlClickListener));
        controlDrawer.setLayoutManager(new LinearLayoutManager(this));
        controlDrawer.setAdapter(mControlAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }
            toolbar.setOnMenuItemClickListener(mMenuListener);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_open);
            mDrawerLayout.addDrawerListener(mDrawerToggle);
            toolbar.inflateMenu(R.menu.activity_omnios);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                mRequestedPermissions.add(READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) != PERMISSION_GRANTED) {
                mRequestedPermissions.add(READ_PHONE_STATE);
            }
            if (!mRequestedPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, mRequestedPermissions.toArray(new String[0]), PermissionRequestCode.REQ_CODE);
            }
        }
        registerBroadcastReceiver();
        bindService(new Intent(this, OmniosService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mService != null) {
                    Message message = mHandler.obtainMessage();
                    message.setData(mService.getCurrentTime());
                    mHandler.sendMessage(message);
                }
            }
        };
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(mTimerTask, 1000L, 1000L);
        mFolderFragment.invalidateList();
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
            if (!granted.isEmpty() && granted.size() == mRequestedPermissions.size()) {
                finish();
                startActivity(getIntent());
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkToPlay(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
        mTimer.cancel();
        if (mService != null) {
            unbindService(mConnection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_omnios, menu);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int iconSize = (int) (20 * dm.density);

        MenuItem menuStop = menu.findItem(R.id.menu_stop);
        menuStop.setIcon(new PauseWhiteIcon(iconSize).getDrawable(getResources()));

        mMenuRemember = menu.findItem(R.id.menu_continue);
        mMenuRemember.setIcon(new BookWhiteIcon(iconSize).getDrawable(getResources()));
        mMenuRemember.setVisible(true);

        mMenuClear = menu.findItem(R.id.menu_clear);
        mMenuClear.setIcon(new ClearWhiteIcon(iconSize).getDrawable(getResources()));
        mMenuClear.setVisible(false);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
        if (mFolderFragment.isVisible() && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
        mMenuClear.setVisible(false);
        mMenuRemember.setVisible(true);
    }

    @Override
    public void play(Playable playable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, OmniosService.class));
        } else {
            startService(new Intent(this, OmniosService.class));
        }
        if (mService != null) {
            mService.play(playable);
        }
    }

    @Override
    public void stop() {
        if (mService != null) {
            mService.stopPlayback();
            mService.setPlayNotifier(true);
        }
    }

    @Override
    public void seek(int progress) {
        if (mService != null) {
            mService.seekPercent(progress);
        }
    }

    @Override
    public void seekLeft(int sec) {
        if (mService != null) {
            mService.seekLeft(sec);
        }
    }

    @Override
    public void seekRight(int sec) {
        if (mService != null) {
            mService.seekRight(sec);
        }
    }

    @Override
    public void addToQueue(String path) {
        if (mService != null) {
            mService.addToQueue(path);
        }
    }

    @Override
    public void removeFromQueue(String path) {
        if (mService != null) {
            mService.removeFromQueue(path);
        }
    }

    @Override
    public Collection<Playable> getQueueCopy() {
        if (mService != null) {
            return mService.getQueueCopy();
        }
        return new ArrayList<>();
    }

    private void checkToPlay(Intent intent) {
        String mUrl = Configures.getRealPathFromURI(this, intent.getData());
        if (mUrl != null && !mUrl.isEmpty()) {
            mFolderFragment.navigateToPath(mUrl);
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
                    case Actions.INVALIDATE:
                        if (mFolderFragment.isVisible()) {
                            mFolderFragment.invalidateList();
                        }
                        if (mQueueFragment.isVisible()) {
                            mQueueFragment.updateData();
                        }
                        break;
                    case Actions.ERROR_PLAYING:
                        if (mFolderFragment.isVisible()) {
                            mFolderFragment.invalidateList();
                        }
                        if (mQueueFragment.isVisible()) {
                            mQueueFragment.updateData();
                        }
                        Toast.makeText(OmniosActivity.this, R.string.cant_play, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Actions.CATEGORY_BROADCAST);
        filter.addAction(Actions.ERROR_PLAYING);
        filter.addAction(Actions.INVALIDATE);
        registerReceiver(mReceiver, filter);
    }

    public void navigateToPath(String path) {
        mFolderFragment.navigateToPath(path);
    }
}