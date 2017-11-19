package com.quewelcy.omnios.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.quewelcy.omnios.R;

public abstract class CircledIcon extends View {

    private final Paint mPaint = new Paint();
    private int mX = 0;
    private int mY = 0;
    private int mRadius = 0;
    private boolean isCircled = true;

    public CircledIcon(Context context) {
        super(context);
    }

    public CircledIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircledIcon);
        isCircled = a.getBoolean(R.styleable.CircledIcon_circled, true);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isCircled) {
            canvas.drawCircle(mX, mY, mRadius, mPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        buildPath();
    }

    protected void create() {
        setWillNotDraw(false);

        mPaint.setStyle(Style.FILL);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xFFFFFFFF);
    }

    protected void buildPath() {
        mX = getWidth() / 2;
        mY = getHeight() / 2;
        mRadius = Math.min(mX, mY);
    }
}