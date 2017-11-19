package com.quewelcy.omnios.view.mono;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public class NextIcon extends WhiteIcon {

    private final Paint mPaint1 = new Paint();
    private final Paint mPaint2 = new Paint();
    private final Paint mPaint3 = new Paint();
    private final Paint mPaint4 = new Paint();

    private final Path mPath1 = new Path();
    private final Path mPath2 = new Path();
    private final Path mPath3 = new Path();
    private final Path mPath4 = new Path();

    public NextIcon(int size) {
        super(size);
    }

    @Override
    protected void draw(Canvas canvas) {
        mPaint1.setStyle(Style.FILL);
        mPaint1.setDither(true);
        mPaint1.setAntiAlias(true);

        mPaint2.setStyle(Style.FILL);
        mPaint2.setDither(true);
        mPaint2.setAntiAlias(true);

        mPaint3.setStyle(Style.FILL);
        mPaint3.setDither(true);
        mPaint3.setAntiAlias(true);

        mPaint4.setStyle(Style.FILL);
        mPaint4.setDither(true);
        mPaint4.setAntiAlias(true);

        mPath1.reset();
        mPath1.moveTo(0.1f * width, 0);
        mPath1.lineTo(0.9f * width, 0.4f * height);
        mPath1.lineTo(0.2f * width, height);
        mPath1.lineTo(0.1f * width, 0);

        mPath2.reset();
        mPath2.moveTo(0.1f * width, 0);
        mPath2.lineTo(0.9f * width, 0.4f * height);
        mPath2.lineTo(0.61f * width, 0.65f * height);
        mPath2.lineTo(0.1f * width, 0);

        mPath3.reset();
        mPath3.moveTo(0.1f * width, 0);
        mPath3.lineTo(0.9f * width, 0.4f * height);
        mPath3.lineTo(0.15f * width, 0.5f * height);
        mPath3.lineTo(0.1f * width, 0);

        mPath4.reset();
        mPath4.moveTo(0.8f * width, 0);
        mPath4.lineTo(width, 0);
        mPath4.lineTo(width, height);
        mPath4.lineTo(0.8f * width, height);
        mPath4.lineTo(0.8f * width, 0);

        mPaint1.setColor(0xFF4FC3F7);
        mPaint2.setColor(0xFF336FBB);
        mPaint3.setColor(0xFFB3E5FC);
        mPaint4.setColor(0xFFB3E5FC);

        canvas.drawPath(mPath1, mPaint1);
        canvas.drawPath(mPath2, mPaint2);
        canvas.drawPath(mPath3, mPaint3);
        canvas.drawPath(mPath4, mPaint4);
    }
}