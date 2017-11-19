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

import com.quewelcy.omnios.R;
import com.quewelcy.omnios.adapters.DetailsListAdapter;
import com.quewelcy.omnios.data.Playable;

import java.util.Collection;

import static android.support.v7.widget.helper.ItemTouchHelper.END;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.START;

public class QueueFragment extends FixedWidthFragment {

    private TextView mNoFiles;
    private DetailsListAdapter mAdapter;
    private ServiceCommunicator mServiceCommunicator;

    private final SimpleCallback mSimpleItemTouchCallback = new SimpleCallback(0, LEFT | START | RIGHT | END) {

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
            return makeMovementFlags(0, LEFT | START | RIGHT | END);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(ViewHolder viewHolder, int direction) {
            switch (direction) {
                case LEFT:
                case START:
                case RIGHT:
                case END:
                    mAdapter.removeByPosition(viewHolder.getAdapterPosition());
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_details_list, container);

        mNoFiles = view.findViewById(R.id.fragment_details_list_empty);
        mNoFiles.setText(R.string.queue_is_empty);
        mAdapter = new DetailsListAdapter(getActivity()) {
            @Override
            public Collection<Playable> getDataSet() {
                return mServiceCommunicator.getQueueCopy();
            }

            @Override
            public void removeItemFromDatSet(Playable playable) {
                mServiceCommunicator.removeFromQueue(playable.getPath());
            }
        };
        mRecyclerView = view.findViewById(R.id.fragment_details_list_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity()));
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

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    public void updateData() {
        mNoFiles.setVisibility(mAdapter.updateDataAndCheckEmpty() ? View.VISIBLE : View.GONE);
    }

    public void setServiceCommunicator(ServiceCommunicator serviceCommunicator) {
        this.mServiceCommunicator = serviceCommunicator;
    }
}