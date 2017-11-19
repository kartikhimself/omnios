package com.quewelcy.omnios.view.mono;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;

import com.quewelcy.omnios.view.CircledIcon;

public class BookIcon extends CircledIcon {

    private final Paint mPaint1 = new Paint();
    private final Paint mPaint2 = new Paint();
    private final Paint mPaint3 = new Paint();
    private final Paint mPaint4 = new Paint();

    private final Path mPath1 = new Path();
    private final Path mPath2 = new Path();
    private final Path mPath3 = new Path();
    private final Path mPath4 = new Path();

    public BookIcon(Context context) {
        super(context);
        create();
    }

    public BookIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath1, mPaint1);
        canvas.drawPath(mPath3, mPaint3);
        canvas.drawPath(mPath2, mPaint2);
        canvas.drawPath(mPath4, mPaint4);
    }

    @Override
    protected void create() {
        super.create();

        mPaint1.setStyle(Style.FILL);
        mPaint1.setDither(true);
        mPaint1.setAntiAlias(true);
        mPaint1.setColor(0xFFFFC107);

        mPaint2.setStyle(Style.FILL);
        mPaint2.setDither(true);
        mPaint2.setAntiAlias(true);
        mPaint2.setColor(0xFFFFB300);

        mPaint3.setStyle(Style.FILL);
        mPaint3.setDither(true);
        mPaint3.setAntiAlias(true);
        mPaint3.setColor(0xFFEEFF41);

        mPaint4.setStyle(Style.FILL);
        mPaint4.setDither(true);
        mPaint4.setAntiAlias(true);
        mPaint4.setColor(0xFFFDD835);

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
        mPath1.moveTo(0.3f * w, 0.15f * h);
        mPath1.lineTo(0.7f * w, 0.15f * h);
        mPath1.lineTo(0.7f * w, 0.85f * h);
        mPath1.lineTo(0.5f * w, 0.7f * h);
        mPath1.lineTo(0.3f * w, 0.85f * h);
        mPath1.lineTo(0.3f * w, 0.15f * h);

        mPath2.reset();
        mPath2.moveTo(0.3f * w, 0.15f * h);
        mPath2.lineTo(0.7f * w, 0.15f * h);
        mPath2.lineTo(0.3f * w, 0.4f * h);
        mPath2.lineTo(0.3f * w, 0.15f * h);

        mPath3.reset();
        mPath3.moveTo(0.3f * w, 0.15f * h);
        mPath3.lineTo(0.4f * w, 0.15f * h);
        mPath3.lineTo(0.7f * w, 0.5f * h);
        mPath3.lineTo(0.3f * w, 0.3f * h);
        mPath3.lineTo(0.3f * w, 0.15f * h);

        mPath4.reset();
        mPath4.moveTo(0.3f * w, 0.6f * h);
        mPath4.lineTo(0.7f * w, 0.45f * h);
        mPath4.lineTo(0.7f * w, 0.6f * h);
        mPath4.lineTo(0.3f * w, 0.85f * h);
        mPath4.lineTo(0.3f * w, 0.7f * h);
    }
}