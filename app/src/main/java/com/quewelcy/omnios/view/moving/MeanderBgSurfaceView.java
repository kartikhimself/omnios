package com.quewelcy.omnios.view.moving;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceView;

import java.util.Timer;
import java.util.TimerTask;

public class MeanderBgSurfaceView extends SurfaceView {

    private static final int MAX_LINES = 10;
    private static final int L = 30;
    private static final int FIGURE_SIZE = L * 5;
    private static final int[] POINT_X = {
            4 * L,
            4 * L,
            2 * L,
            2 * L,
            3 * L,
            3 * L,
            L,
            L,
            5 * L,
            5 * L

    };
    private static final int[] POINT_Y = {
            0,
            -3 * L,
            -3 * L,
            -2 * L,
            -2 * L,
            -1 * L,
            -1 * L,
            -4 * L,
            -4 * L,
            0
    };
    private static final int[] COLORS = {
            0xFFFFA0FD,
            0xFFDDD92A,
            0xFF52AD9C,
            0xFFFFCBDD,
            0xFF829298,
            0xFFA26769,
            0xFFFFBF46,
            0xFFDDD92A,
            0xFFE49273,
            0xFF734B5E
    };

    private int mStartX = 0;
    private int mStartY = 0;
    private int mFigurePos = 0;
    private int mLinePos = 0;

    private Paint mFgPaint = new Paint();
    private Paint mBgPaint = new Paint();
    private Path mPath = new Path();
    private Timer mTimer = new Timer();

    public MeanderBgSurfaceView(Context context) {
        super(context);
        create();
    }

    public MeanderBgSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    public MeanderBgSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), mBgPaint);
        canvas.drawPath(mPath, mFgPaint);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        resetPath();
    }

    public void showSplash() {
        setWillNotDraw(false);
    }

    public void clearSplash() {
        setWillNotDraw(true);
    }


    protected void create() {
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(getColor());

        mFgPaint.setStyle(Paint.Style.STROKE);
        mFgPaint.setStrokeWidth(3);
        mFgPaint.setDither(true);
        mFgPaint.setAntiAlias(true);
        mFgPaint.setColor(0xF0FFFFFF);

        long reCalcTimeout = 50;
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mStartY == 0) {
                    return;
                }
                lineByLine();
            }
        }, reCalcTimeout, reCalcTimeout);
    }

    private void lineByLine() {
        int x = mStartX + mFigurePos * FIGURE_SIZE + POINT_X[mLinePos];
        int y = mStartY + POINT_Y[mLinePos];
        mPath.lineTo(x, y);

        mLinePos++;
        if (mLinePos >= MAX_LINES) {
            mLinePos = 0;
            mFigurePos++;
            if (x > getWidth() || y > getHeight()) {
                mFigurePos = 0;
                resetPath();
            }
        }
    }

    private int getColor() {
        int index;
        do {
            index = (int) (Math.random() * COLORS.length);
        } while (index < 0 || index >= COLORS.length);
        return COLORS[index];
    }

    private void resetPath() {
        mStartX = 0;
        mStartY = (int) (getHeight() * 0.9);

        mPath.reset();
        mPath.moveTo(mStartX, mStartY);
    }
}