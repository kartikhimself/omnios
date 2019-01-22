package com.quewelcy.omnios.fragments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import static android.support.v7.widget.RecyclerView.OnItemTouchListener;
import static android.view.GestureDetector.SimpleOnGestureListener;

public class RecyclerItemClickListener implements OnItemTouchListener {

    private final OnItemClickListener mClickListener;
    private final GestureDetector mGestureDetector;
    private View childView;
    private int childViewPosition;

    RecyclerItemClickListener(Context context) {
        this(context, null);
    }

    public RecyclerItemClickListener(Context context, OnItemClickListener clickListener) {
        mClickListener = clickListener;
        mGestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                if (childView != null && mClickListener != null) {
                    mClickListener.onItemClick(childViewPosition);
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent event) {
                if (childView != null && mClickListener != null) {
                    mClickListener.onItemLongClick(childViewPosition);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent event) {
        childView = view.findChildViewUnder(event.getX(), event.getY());
        if (childView != null) {
            childViewPosition = view.getChildAdapterPosition(childView);
        }
        return childView != null && mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent event) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position);
    }
}