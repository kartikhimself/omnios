package com.quewelcy.omnios.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.quewelcy.omnios.view.pattern.PaperStack;

public class BgSurfaceView extends SurfaceView {

    private Bitmap splashBitmap;

    public BgSurfaceView(Context context) {
        super(context);
    }

    public BgSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BgSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (splashBitmap != null) {
            canvas.drawBitmap(splashBitmap, 0, 0, null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        splashBitmap = new PaperStack(getWidth(), getHeight()).getBitmap();
    }

    public void showSplash() {
        setWillNotDraw(false);
    }

    public void clearSplash() {
        setWillNotDraw(true);
    }
}