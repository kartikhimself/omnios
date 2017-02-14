package com.quewelcy.omnios.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;

public class MusicIcon extends CircledIcon {

    private Paint mPaint1 = new Paint();
    private Paint mPaint2 = new Paint();
    private Paint mPaint3 = new Paint();

    private Path mPath1 = new Path();
    private Path mPath2 = new Path();
    private Path mPath3 = new Path();

    public MusicIcon(Context context) {
        super(context);
        create();
    }

    public MusicIcon(Context context, AttributeSet attrs) {
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
        mPaint1.setColor(0xFF4FC3F7);

        mPaint2.setStyle(Style.FILL);
        mPaint2.setDither(true);
        mPaint2.setAntiAlias(true);
        mPaint2.setColor(0xFF336FBB);

        mPaint3.setStyle(Style.FILL);
        mPaint3.setDither(true);
        mPaint3.setAntiAlias(true);
        mPaint3.setColor(0xFFB3E5FC);

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
        mPath1.moveTo(0.35f * w, 0.15f * h);
        mPath1.lineTo(0.85f * w, 0.5f * h);
        mPath1.lineTo(0.35f * w, 0.85f * h);
        mPath1.lineTo(0.35f * w, 0.15f * h);

        mPath2.reset();
        mPath2.moveTo(0.35f * w, 0.15f * h);
        mPath2.lineTo(0.85f * w, 0.5f * h);
        mPath2.lineTo(0.35f * w, 0.55f * h);
        mPath2.lineTo(0.35f * w, 0.15f * h);

        mPath3.reset();
        mPath3.moveTo(0.35f * w, 0.15f * h);
        mPath3.lineTo(0.85f * w, 0.5f * h);
        mPath3.lineTo(0.61f * w, 0.67f * h);
        mPath3.lineTo(0.35f * w, 0.15f * h);
    }
}