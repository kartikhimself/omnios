package com.quewelcy.omnios.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;

public class VideoIcon extends CircledIcon {

    private Paint mPaint0 = new Paint();
    private Paint mPaint1 = new Paint();
    private Paint mPaint2 = new Paint();
    private Paint mPaint3 = new Paint();

    private Path mPath0 = new Path();
    private Path mPath1 = new Path();
    private Path mPath2 = new Path();
    private Path mPath3 = new Path();

    public VideoIcon(Context context) {
        super(context);
        create();
    }

    public VideoIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath0, mPaint0);
        canvas.drawPath(mPath2, mPaint2);
        canvas.drawPath(mPath1, mPaint1);
        canvas.drawPath(mPath3, mPaint3);
    }

    @Override
    protected void create() {
        super.create();

        mPaint0.setStyle(Style.FILL);
        mPaint0.setDither(true);
        mPaint0.setAntiAlias(true);

        mPaint1.setStyle(Style.FILL);
        mPaint1.setDither(true);
        mPaint1.setAntiAlias(true);

        mPaint2.setStyle(Style.FILL);
        mPaint2.setDither(true);
        mPaint2.setAntiAlias(true);

        mPaint3.setStyle(Style.FILL);
        mPaint3.setDither(true);
        mPaint3.setAntiAlias(true);

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

        mPath0.reset();
        mPath0.moveTo(0.2f * w, 0.2f * h);
        mPath0.lineTo(0.8f * w, 0.2f * h);
        mPath0.lineTo(0.8f * w, 0.8f * h);
        mPath0.lineTo(0.2f * w, 0.8f * h);
        mPath0.lineTo(0.2f * w, 0.2f * h);

        mPath1.reset();
        mPath1.moveTo(0.2f * w, 0.2f * h);
        mPath1.lineTo(0.25f * w, 0.2f * h);
        mPath1.lineTo(0.35f * w, 0.8f * h);
        mPath1.lineTo(0.2f * w, 0.8f * h);
        mPath1.lineTo(0.2f * w, 0.2f * h);

        mPath2.reset();
        mPath2.moveTo(0.2f * w, 0.2f * h);
        mPath2.lineTo(0.8f * w, 0.55f * h);
        mPath2.lineTo(0.8f * w, 0.2f * h);
        mPath2.lineTo(0.2f * w, 0.2f * h);

        mPath3.reset();
        mPath3.moveTo(0.3f * w, 0.8f * h);
        mPath3.lineTo(0.8f * w, 0.4f * h);
        mPath3.lineTo(0.8f * w, 0.8f * h);
        mPath3.lineTo(0.3f * w, 0.8f * h);

        mPaint0.setColor(0xFFAB47BC);
        mPaint1.setColor(0xFFCE93D8);
        mPaint2.setColor(0xFFE1BEE7);
        mPaint3.setColor(0xFFEDE0F0);
    }
}