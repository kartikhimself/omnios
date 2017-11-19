package com.quewelcy.omnios.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quewelcy.omnios.Configures;
import com.quewelcy.omnios.R;
import com.quewelcy.omnios.data.Playable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.support.v7.widget.RecyclerView.ViewHolder;

public abstract class DetailsListAdapter extends RecyclerView.Adapter<ViewHolder> {

    private final LayoutInflater mInflater;
    private final List<Playable> mDataSet;

    protected DetailsListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mDataSet = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DetailsListViewHolder(mInflater.inflate(R.layout.item_continue, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String path = mDataSet.get(position).getPath();
        if (path == null || path.isEmpty()) {
            return;
        }
        int index = path.lastIndexOf(File.separator);
        if (index < 0) {
            return;
        }
        TextView title = holder.itemView.findViewById(R.id.item_continue_title);
        title.setText(Configures.dropExtension(path.substring(index + 1)));

        TextView pathTo = holder.itemView.findViewById(R.id.item_continue_path_to);
        pathTo.setText(path.substring(0, index));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public Playable getPlayable(int position) {
        return mDataSet.get(position);
    }

    public void removeByPosition(int position) {
        if (position < 0 || position >= mDataSet.size()) {
            return;
        }
        Playable playable = mDataSet.remove(position);
        if (playable == null) {
            return;
        }
        removeItemFromDatSet(playable);
        notifyDataSetChanged();
    }

    public boolean updateDataAndCheckEmpty() {
        mDataSet.clear();
        mDataSet.addAll(getDataSet());
        notifyDataSetChanged();
        return mDataSet.isEmpty();
    }

    public abstract Collection<Playable> getDataSet();

    public abstract void removeItemFromDatSet(Playable playable);

    private static class DetailsListViewHolder extends ViewHolder {
        public final View view;

        DetailsListViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }
}