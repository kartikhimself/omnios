package com.quewelcy.omnios.view.mono;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public class ClearWhiteIcon extends WhiteIcon {

    private Paint mPaint = new Paint();

    private Path mPath1 = new Path();
    private Path mPath2 = new Path();
    private Path mPath3 = new Path();

    public ClearWhiteIcon(int size) {
        super(size);
    }

    @Override
    protected void draw(Canvas canvas) {

        mPaint.setStyle(Style.FILL);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);

        mPath1.reset();
        mPath1.moveTo(0.1f * width, 0.1f * height);
        mPath1.lineTo(0.4f * width, 0.1f * height);
        mPath1.lineTo(0.4f * width, 0.0f * height);
        mPath1.lineTo(0.6f * width, 0.0f * height);
        mPath1.lineTo(0.6f * width, 0.1f * height);
        mPath1.lineTo(0.9f * width, 0.1f * height);
        mPath1.lineTo(0.9f * width, 0.2f * height);
        mPath1.lineTo(0.1f * width, 0.2f * height);
        mPath1.lineTo(0.1f * width, 0.1f * height);

        mPath2.reset();
        mPath2.moveTo(0.1f * width, 0.3f * height);
        mPath2.lineTo(0.9f * width, 0.3f * height);
        mPath2.lineTo(0.8f * width, 0.9f * height);
        mPath2.lineTo(0.2f * width, 0.9f * height);
        mPath2.lineTo(0.1f * width, 0.3f * height);

        mPath3.reset();
        mPath3.moveTo(0.1f * width, 0.3f * height);
        mPath3.lineTo(0.85f * width, 0.6f * height);
        mPath3.lineTo(0.8f * width, 0.9f * height);
        mPath3.lineTo(0.2f * width, 0.9f * height);
        mPath3.lineTo(0.1f * width, 0.3f * height);

        canvas.drawPath(mPath1, mPaint);
        canvas.drawPath(mPath2, mPaint);
        canvas.drawPath(mPath3, mPaint);
    }
}