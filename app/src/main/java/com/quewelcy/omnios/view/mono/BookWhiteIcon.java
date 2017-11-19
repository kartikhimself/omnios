package com.quewelcy.omnios.view.mono;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public class BookWhiteIcon extends WhiteIcon {

    private final Paint mPaint = new Paint();

    private final Path mPath1 = new Path();
    private final Path mPath2 = new Path();
    private final Path mPath3 = new Path();
    private final Path mPath4 = new Path();

    public BookWhiteIcon(int size) {
        super(size);
    }

    @Override
    protected void draw(Canvas canvas) {

        mPaint.setStyle(Style.FILL);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);

        mPath1.reset();
        mPath1.moveTo(0.2f * width, 0);
        mPath1.lineTo(0.8f * width, 0);
        mPath1.lineTo(0.8f * width, height);
        mPath1.lineTo(0.5f * width, 0.8f * height);
        mPath1.lineTo(0.2f * width, height);
        mPath1.lineTo(0.2f * width, 0);

        mPath2.reset();
        mPath2.moveTo(0.2f * width, 0);
        mPath2.lineTo(0.8f * width, 0);
        mPath2.lineTo(0.2f * width, 0.4f * height);
        mPath2.lineTo(0.2f * width, 0);

        mPath3.reset();
        mPath3.moveTo(0.2f * width, 0);
        mPath3.lineTo(0.4f * width, 0);
        mPath3.lineTo(0.8f * width, 0.5f * height);
        mPath3.lineTo(0.2f * width, 0.8f * height);
        mPath3.lineTo(0.2f * width, 0);

        mPath4.reset();
        mPath4.moveTo(0.2f * width, 0.6f * height);
        mPath4.lineTo(0.8f * width, 0.45f * height);
        mPath4.lineTo(0.8f * width, 0.6f * height);
        mPath4.lineTo(0.2f * width, height);
        mPath4.lineTo(0.2f * width, 0.6f * height);

        canvas.drawPath(mPath1, mPaint);
        canvas.drawPath(mPath3, mPaint);
        canvas.drawPath(mPath2, mPaint);
        canvas.drawPath(mPath4, mPaint);
    }
}