package com.quewelcy.omnios.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quewelcy.omnios.OmniosActivity;
import com.quewelcy.omnios.R;
import com.quewelcy.omnios.adapters.DetailsListAdapter;
import com.quewelcy.omnios.data.Playable;
import com.quewelcy.omnios.data.PrefHelper;
import com.quewelcy.omnios.fragments.RecyclerItemClickListener.OnItemClickListener;

import java.util.Collection;

public class SavedPathsFragment extends FixedWidthFragment {

    private TextView mNoFiles;
    private DetailsListAdapter mAdapter;

    private OnItemClickListener mClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if (position < 0) {
                return;
            }
            Playable playable = mAdapter.getPlayable(position);
            if (playable == null) {
                return;
            }
            getActivity().onBackPressed();
            ((OmniosActivity) getActivity()).navigateToPath(playable.getPath());
        }

        @Override
        public void onItemLongClick(int position) {

        }
    };

    private SimpleCallback mSimpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.END);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              ViewHolder viewHolder,
                              ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(ViewHolder viewHolder, int direction) {
            switch (direction) {
                case ItemTouchHelper.RIGHT:
                case ItemTouchHelper.END:
                    mAdapter.removeByPosition(viewHolder.getAdapterPosition());
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_details_list, container);

        mNoFiles = (TextView) view.findViewById(R.id.fragment_details_list_empty);
        mNoFiles.setText(R.string.no_saved_paths);
        mAdapter = new DetailsListAdapter(getActivity()) {
            @Override
            public Collection<Playable> getDataSet() {
                return PrefHelper.getPerms(getContext()).values();
            }

            @Override
            public void removeItemFromDatSet(Playable playable) {
                PrefHelper.removeFromPerms(getContext(), playable);
            }
        };
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_details_list_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mClickListener));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mSimpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        setRetainInstance(true);
    }

    public void updateData() {
        mNoFiles.setVisibility(mAdapter.updateDataAndCheckEmpty() ? View.VISIBLE : View.GONE);
    }
}