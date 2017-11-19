package com.quewelcy.omnios.view.mono;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public class PlayIcon extends WhiteIcon {

    private final Paint mPaint1 = new Paint();
    private final Paint mPaint2 = new Paint();
    private final Paint mPaint3 = new Paint();

    private final Path mPath1 = new Path();
    private final Path mPath2 = new Path();
    private final Path mPath3 = new Path();

    public PlayIcon(int size) {
        super(size);
    }

    @Override
    protected void draw(Canvas canvas) {
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

        mPath1.reset();
        mPath1.moveTo(0.1f * width, 0);
        mPath1.lineTo(0.9f * width, 0.4f * height);
        mPath1.lineTo(0.2f * width, height);
        mPath1.lineTo(0.1f * width, 0);

        mPath2.reset();
        mPath2.moveTo(0.1f * width, 0);
        mPath2.lineTo(0.9f * width, 0.4f * height);
        mPath2.lineTo(0.15f * width, 0.5f * height);
        mPath2.lineTo(0.1f * width, 0);

        mPath3.reset();
        mPath3.moveTo(0.1f * width, 0);
        mPath3.lineTo(0.9f * width, 0.4f * height);
        mPath3.lineTo(0.61f * width, 0.65f * height);
        mPath3.lineTo(0.1f * width, 0);

        canvas.drawPath(mPath1, mPaint1);
        canvas.drawPath(mPath2, mPaint2);
        canvas.drawPath(mPath3, mPaint3);
    }
}