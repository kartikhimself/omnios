package com.quewelcy.omnios.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.quewelcy.omnios.view.pattern.Mosaic;

public class MosaicIcon extends CircledIcon {

    private Bitmap bm;

    public MosaicIcon(Context context) {
        super(context);
        create();
    }

    public MosaicIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bm != null) {
            canvas.drawBitmap(bm, 0, 0, null);
        }
    }

    @Override
    protected void create() {
        super.create();
        buildPath();
    }

    @Override
    protected void buildPath() {
        super.buildPath();

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        bm = new Mosaic(w, h).getBitmap();
    }
}