package com.quewelcy.omnios.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public class FixedWidthFragment extends Fragment {

    protected RecyclerView mRecyclerView;

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        Configuration configuration = getResources().getConfiguration();
        int layoutSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if ((layoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                layoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) &&
                configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            int widthPixels = getResources().getDisplayMetrics().widthPixels;
            ViewGroup.LayoutParams layoutParams = mRecyclerView.getLayoutParams();
            layoutParams.width = (int) (widthPixels * 0.7);
            mRecyclerView.setLayoutParams(layoutParams);
        }
    }
}