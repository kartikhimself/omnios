package com.quewelcy.omnios.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;

public class FolderIcon extends CircledIcon {

    private Paint mPaint1 = new Paint();
    private Paint mPaint2 = new Paint();
    private Paint mPaint3 = new Paint();

    private Path mPath1 = new Path();
    private Path mPath2 = new Path();
    private Path mPath3 = new Path();

    public FolderIcon(Context context) {
        super(context);
        create();
    }

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath1, mPaint1);
        canvas.drawPath(mPath2, mPaint2);
        canvas.drawPath(mPath3, mPaint3);
    }

    @Override
    protected void create() {
        super.create();

        mPaint1.setStyle(Style.FILL);
        mPaint1.setDither(true);
        mPaint1.setAntiAlias(true);
        mPaint1.setColor(0xFFFF8A65);

        mPaint2.setStyle(Style.FILL);
        mPaint2.setDither(true);
        mPaint2.setAntiAlias(true);
        mPaint2.setColor(0xFFFF7043);

        mPaint3.setStyle(Style.FILL);
        mPaint3.setDither(true);
        mPaint3.setAntiAlias(true);
        mPaint3.setColor(0xFFFFAB91);

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

        mPath1.reset();
        mPath1.moveTo(0.15f * w, 0.2f * h);
        mPath1.lineTo(0.4f * w, 0.2f * h);
        mPath1.lineTo(0.5f * w, 0.3f * h);
        mPath1.lineTo(0.85f * w, 0.3f * h);
        mPath1.lineTo(0.85f * w, 0.8f * h);
        mPath1.lineTo(0.15f * w, 0.8f * h);
        mPath1.lineTo(0.15f * w, 0.3f * h);

        mPath2.reset();
        mPath2.moveTo(0.15f * w, 0.4f * h);
        mPath2.lineTo(0.5f * w, 0.6f * h);
        mPath2.lineTo(0.5f * w, 0.8f * h);
        mPath2.lineTo(0.15f * w, 0.8f * h);
        mPath2.lineTo(0.15f * w, 0.4f * h);

        mPath3.reset();
        mPath3.moveTo(0.4f * w, 0.8f * h);
        mPath3.lineTo(0.6f * w, 0.3f * h);
        mPath3.lineTo(0.85f * w, 0.3f * h);
        mPath3.lineTo(0.85f * w, 0.8f * h);
        mPath3.lineTo(0.4f * w, 0.8f * h);
    }
}