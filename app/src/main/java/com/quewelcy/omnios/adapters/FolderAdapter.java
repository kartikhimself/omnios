package com.quewelcy.omnios.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quewelcy.omnios.Configures;
import com.quewelcy.omnios.R;
import com.quewelcy.omnios.data.PrefHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.ContextCompat.getColor;

public class FolderAdapter extends Adapter<ViewHolder> {

    private final LayoutInflater mInflater;
    private final List<File> mFiles;
    private final Context mContext;

    public FolderAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mFiles = new ArrayList<>();
    }

    @Override
    public ReViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReViewHolder(mInflater.inflate(R.layout.item_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RelativeLayout bg = holder.itemView.findViewById(R.id.item_folder_bg);
        TextView title = holder.itemView.findViewById(R.id.item_folder_title);
        View musicIcon = holder.itemView.findViewById(R.id.item_folder_music_icon);
        View videoIcon = holder.itemView.findViewById(R.id.item_folder_video_icon);
        View folderIcon = holder.itemView.findViewById(R.id.item_folder_folder_icon);
        View bookIcon = holder.itemView.findViewById(R.id.item_folder_book_icon);

        File item = mFiles.get(position);
        String currentPath = PrefHelper.getCurrentPlayable(mContext).getPath();
        title.setText(Configures.dropExtension(item.getName()));

        if (item.isDirectory()) {
            folderIcon.setVisibility(View.VISIBLE);
            musicIcon.setVisibility(View.GONE);
            videoIcon.setVisibility(View.GONE);
            bookIcon.setVisibility(View.GONE);
            if (currentPath.startsWith(item.getAbsolutePath())) {
                bg.setBackgroundResource(R.drawable.navigation_active_cl);
                title.setTextColor(getColor(mContext, R.color.active_text));
            } else {
                bg.setBackgroundResource(R.drawable.navigation_normal_cl);
                title.setTextColor(getColor(mContext, R.color.normal_text));
            }
        } else {
            if (Configures.isMusic(item.getPath().toLowerCase())) {
                if (PrefHelper.isPersisted(mContext, item.getAbsolutePath())) {
                    bookIcon.setVisibility(View.VISIBLE);
                    musicIcon.setVisibility(View.GONE);
                } else {
                    musicIcon.setVisibility(View.VISIBLE);
                    bookIcon.setVisibility(View.GONE);
                }
                folderIcon.setVisibility(View.GONE);
                videoIcon.setVisibility(View.GONE);
            } else {
                videoIcon.setVisibility(View.VISIBLE);
                folderIcon.setVisibility(View.GONE);
                musicIcon.setVisibility(View.GONE);
                bookIcon.setVisibility(View.GONE);
            }
            if (item.getAbsolutePath().equals(currentPath)) {
                bg.setBackgroundResource(R.drawable.navigation_active_cl);
                title.setTextColor(getColor(mContext, R.color.active_text));
            } else {
                bg.setBackgroundResource(R.drawable.navigation_normal_cl);
                title.setTextColor(getColor(mContext, R.color.normal_text));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public void setFiles(List<File> files) {
        if (files == null) {
            return;
        }
        mFiles.clear();
        mFiles.addAll(files);
        notifyDataSetChanged();
    }

    public File getItem(int position) {
        return mFiles.get(position);
    }

    private static class ReViewHolder extends ViewHolder {
        public final View view;

        ReViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }
}
