package com.quewelcy.omnios.view.mono;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public class PauseIcon extends WhiteIcon {

    private final Paint mPaint1 = new Paint();
    private final Paint mPaint2 = new Paint();
    private final Paint mPaint3 = new Paint();
    private final Paint mPaint4 = new Paint();

    private final Path mPath1 = new Path();
    private final Path mPath2 = new Path();
    private final Path mPath3 = new Path();
    private final Path mPath4 = new Path();

    public PauseIcon(int size) {
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
        mPath1.moveTo(0.1f * width, 0.1f * height);
        mPath1.lineTo(0.4f * width, 0.1f * height);
        mPath1.lineTo(0.4f * width, 0.9f * height);
        mPath1.lineTo(0.1f * width, 0.9f * height);
        mPath1.lineTo(0.1f * width, 0.1f * height);

        mPath2.reset();
        mPath2.moveTo(0.1f * width, 0.2f * height);
        mPath2.lineTo(0.4f * width, 0.6f * height);
        mPath2.lineTo(0.4f * width, 0.9f * height);
        mPath2.lineTo(0.1f * width, 0.9f * height);
        mPath2.lineTo(0.1f * width, 0.2f * height);

        mPath3.reset();
        mPath3.moveTo(0.6f * width, 0.1f * height);
        mPath3.lineTo(0.9f * width, 0.1f * height);
        mPath3.lineTo(0.9f * width, 0.9f * height);
        mPath3.lineTo(0.6f * width, 0.9f * height);
        mPath3.lineTo(0.6f * width, 0.1f * height);

        mPath4.reset();
        mPath4.moveTo(0.6f * width, 0.4f * height);
        mPath4.lineTo(0.9f * width, 0.8f * height);
        mPath4.lineTo(0.9f * width, 0.9f * height);
        mPath4.lineTo(0.6f * width, 0.9f * height);
        mPath4.lineTo(0.6f * width, 0.4f * height);

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