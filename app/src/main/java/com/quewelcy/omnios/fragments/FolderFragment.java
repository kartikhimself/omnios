package com.quewelcy.omnios.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.quewelcy.omnios.Configures;
import com.quewelcy.omnios.R;
import com.quewelcy.omnios.VideoActivity;
import com.quewelcy.omnios.adapters.FolderAdapter;
import com.quewelcy.omnios.data.Playable;
import com.quewelcy.omnios.data.PrefHelper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.quewelcy.omnios.Configures.DirFileComparator.DIR_FILE_SORT;
import static com.quewelcy.omnios.Configures.DirFileComparator.NAME_SORT;
import static com.quewelcy.omnios.Configures.DirFileComparator.ascending;
import static com.quewelcy.omnios.Configures.DirFileComparator.getComparator;
import static com.quewelcy.omnios.fragments.RecyclerItemClickListener.OnItemClickListener;

public class FolderFragment extends FixedWidthFragment {

    private HorizontalScrollView mPathScroller;
    private ServiceCommunicator mServiceCommunicator;
    private FolderAdapter mAdapter;
    private LinearLayout mPath;
    private TextView mNoFiles;
    private TextView mTimeCur;
    private TextView mTimeEnd;
    private Context mContext;
    private SeekBar mSeek;

    private final FileFilter mMp3VideoDirFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || (f.isFile() && isFormatSupported(f.getName().toLowerCase()));
        }
    };
    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mServiceCommunicator != null && fromUser) {
                mServiceCommunicator.seek(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private final OnClickListener mNavClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String path = (String) v.getTag();
            if (path != null && path.length() > 0) {
                readDirAndSet(new File(path));
            }
        }
    };
    private final OnItemClickListener mClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if (position < 0) {
                return;
            }
            selectFile(mAdapter.getItem(position));
        }

        @Override
        public void onItemLongClick(int position) {
            if (position < 0 || mServiceCommunicator == null) {
                return;
            }
            File selected = mAdapter.getItem(position);
            if (selected == null) {
                return;
            }
            if (Configures.isMusic(selected.getAbsolutePath())) {
                mServiceCommunicator.addToQueue(selected.getAbsolutePath());
                Toast.makeText(mContext, R.string.added_to_queue, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void setServiceCommunicator(ServiceCommunicator serviceCommunicator) {
        this.mServiceCommunicator = serviceCommunicator;
    }

    private void selectFile(File selected) {
        if (selected.isDirectory()) {
            readDirAndSet(selected);
        } else if (selected.isFile()) {
            final String p = selected.getPath().toLowerCase();
            if (Configures.isMusic(p)) {
                startPlay(selected);
            } else if (Configures.isVideo(p)) {
                startVideo(selected);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_folder, container);
        mContext = inflater.getContext();
        mAdapter = new FolderAdapter(inflater.getContext());
        mPathScroller = view.findViewById(R.id.fragment_folder_path_scroller);
        mPath = view.findViewById(R.id.fragment_folder_path);
        mNoFiles = view.findViewById(R.id.fragment_folder_empty);
        mTimeCur = view.findViewById(R.id.fragment_folder_cur_time);
        mTimeEnd = view.findViewById(R.id.fragment_folder_end_time);
        mSeek = view.findViewById(R.id.fragment_folder_seek);
        mSeek.setOnSeekBarChangeListener(mSeekListener);

        mRecyclerView = view.findViewById(R.id.fragment_recycler_folder_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mClickListener));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        setRetainInstance(true);
        Playable playable = PrefHelper.getCurrentPlayable(mContext);
        readDirAndSet(new File(playable.getPath()));
    }

    private boolean isFormatSupported(String path) {
        return Configures.isMusic(path) || Configures.isVideo(path);
    }

    private void startPlay(File selected) {
        if (mServiceCommunicator != null) {
            mServiceCommunicator.play(new Playable(Configures.dropExtension(selected.getName()), selected.getAbsolutePath(), 0));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        }, Configures.DELAY_MILLIS);
    }

    private void startVideo(File selected) {
        Intent intent = new Intent(mContext, VideoActivity.class);
        intent.setData(Uri.fromFile(selected));
        startActivity(intent);
    }

    private int readDirAndSet(File file) {
        File newFile = null;
        try {
            newFile = file.getCanonicalFile();
        } catch (IOException e) {
            Toast.makeText(getActivity(), R.string.cant_find_location, Toast.LENGTH_SHORT).show();
        }

        File oldFile = null;
        int oldFileIndex = -1;
        if (newFile == null) {
            return oldFileIndex;
        }
        if (!newFile.exists()) {
            newFile = new File(Environment.getExternalStorageDirectory().getPath());
        }
        if (newFile.isFile()) {
            oldFile = newFile;
            newFile = newFile.getParentFile();
        }
        File[] files = newFile.listFiles(mMp3VideoDirFilter);
        if (files == null) {
            files = new File[0];
        }
        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        Collections.sort(fileList, ascending(getComparator(DIR_FILE_SORT, NAME_SORT)));
        fileList.add(0, new File(newFile.getAbsolutePath() + File.separator + ".." + File.separator));
        oldFileIndex = fileList.indexOf(oldFile);

        mAdapter.setFiles(fileList);
        mNoFiles.setVisibility(fileList.isEmpty() ? View.VISIBLE : View.GONE);

        // Navigation
        String[] paths = newFile.getAbsolutePath().split(File.separator);
        StringBuilder accumulator = new StringBuilder();
        if (mPath.getChildCount() > 0) {
            mPath.removeAllViews();
        }

        for (int i = 1; i < paths.length; i++) {
            accumulator.append(File.separator).append(paths[i]);

            View v = View.inflate(mContext, R.layout.item_navigation, null);
            mPath.addView(v);

            TextView nav = v.findViewById(R.id.item_navigation);
            nav.setText(paths[i]);
            if (i == paths.length - 1) {
                nav.setBackgroundResource(android.R.color.transparent);
            } else {
                nav.setTag(accumulator.toString());
                nav.setOnClickListener(mNavClickListener);
            }
        }

        // Move path
        mPathScroller.post(new Runnable() {
            @Override
            public void run() {
                mPathScroller.smoothScrollTo(mPathScroller.getWidth() + 100, 0);
            }
        });
        return oldFileIndex;
    }

    public void setTime(String timeCur, String timeEnd, int percent) {
        mTimeCur.setText(timeCur);
        mTimeEnd.setText(timeEnd);
        mSeek.setProgress(percent);
    }

    public void resetCurrent() {
        mTimeCur.setText(R.string.zero_time);
        mTimeEnd.setText(R.string.zero_time);
        mSeek.setProgress(0);
    }

    public void invalidateList() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateData() {
        mAdapter.notifyDataSetChanged();
    }

    public void navigateToPath(String path) {
        File file = new File(path);
        int selectedFilePosition = readDirAndSet(file);
        selectFile(file);
        mRecyclerView.getLayoutManager().scrollToPosition(selectedFilePosition);
    }
}
