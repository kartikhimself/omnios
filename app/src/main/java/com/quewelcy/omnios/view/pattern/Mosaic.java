package com.quewelcy.omnios.view.pattern;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

import java.util.HashMap;
import java.util.Map;

public class Mosaic extends MosaicPattern {

    private static final int STEP = 50;

    private final Triangle ts1 = new Triangle(2, 9, 8, 2);
    private final Triangle ts2 = new Triangle(1, 2, 9, 1);
    private final Triangle ts3 = new Triangle(1, 8, 9, 1);
    private final Triangle ts4 = new Triangle(1, 2, 8, 1);

    private final Triangle tb1 = new Triangle(3, 5, 7, 3);
    private final Triangle tb2 = new Triangle(1, 3, 5, 1);
    private final Triangle tb3 = new Triangle(1, 7, 5, 1);
    private final Triangle tb4 = new Triangle(3, 1, 7, 3);

    private final Triangle[] triangles = new Triangle[]{
            ts1, ts2, ts3, ts4, tb1, tb2, tb3, tb4
    };
    private final Map<Triangle, Joint[]> next = new HashMap<>();

    public Mosaic(int weight, int height) {
        super(weight, height);
        next.put(ts1, new Joint[]{new Joint(ts4, 1, 0), new Joint(tb4, 1, -1), new Joint(ts2, 1, 0), new Joint(tb2, 1, 0), new Joint(ts3, 0, 1)});
        next.put(ts2, new Joint[]{new Joint(ts3, 0, 0), new Joint(tb3, 0, 0), new Joint(tb3, -1, -1), new Joint(ts3, 0, 1)});
        next.put(ts3, new Joint[]{new Joint(ts4, 1, 0), new Joint(ts2, 1, 0), new Joint(tb4, 1, 0), new Joint(tb2, 1, 0), new Joint(ts1, 0, -1), new Joint(tb1, 0, -2)});
        next.put(ts4, new Joint[]{new Joint(tb1, -1, 0), new Joint(tb1, 0, -1), new Joint(ts1, 0, 0), new Joint(tb1, 0, -2)});
        next.put(tb1, new Joint[]{new Joint(ts4, 2, 0), new Joint(tb4, 2, 0), new Joint(ts2, 2, 0), new Joint(tb2, 2, 0), new Joint(ts2, 2, 1)});
        next.put(tb2, new Joint[]{new Joint(ts3, 0, 0), new Joint(ts3, 1, 1), new Joint(ts3, 1, 2), new Joint(tb3, 1, 1)});
        next.put(tb3, new Joint[]{new Joint(ts4, 2, 0), new Joint(tb4, 2, -1), new Joint(ts2, 2, 1), new Joint(tb2, 2, 0), new Joint(ts2, 2, 0)});
        next.put(tb4, new Joint[]{new Joint(ts1, 1, 0), new Joint(tb1, 1, -1), new Joint(ts1, 0, 1), new Joint(ts1, 1, -1), new Joint(ts1, 0, -1)});
    }

    @Override
    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint bgPaint = new Paint();
        bgPaint.setStyle(Style.FILL);
        bgPaint.setAntiAlias(true);
        bgPaint.setDither(true);
        bgPaint.setColor(chooseSchemeAndGetColor());

        Paint mosaicPaint = new Paint(bgPaint);
        mosaicPaint.setColor(getNextColor());

        canvas.drawRect(0, 0, width, height, bgPaint);

        int anchorX = STEP;
        int anchorY = height * 3 / 4;

        int startIndex = (int) (Math.random() * triangles.length);
        if (startIndex == triangles.length) {
            startIndex = 0;
        }
        Triangle triangle = triangles[startIndex];
        while (anchorX < width - 2 * STEP && anchorY > STEP) {
            Path path = new Path();
            for (int point : triangle.points) {
                float x = 0, y = 0;
                switch (point) {
                    case 1:
                        x = anchorX;
                        y = anchorY;
                        break;
                    case 2:
                        x = anchorX;
                        y = anchorY + STEP;
                        break;
                    case 3:
                        x = anchorX;
                        y = anchorY + 2 * STEP;
                        break;
                    case 4:
                        x = anchorX + STEP;
                        y = anchorY + 2 * STEP;
                        break;
                    case 5:
                        x = anchorX + 2 * STEP;
                        y = anchorY + 2 * STEP;
                        break;
                    case 6:
                        x = anchorX + 2 * STEP;
                        y = anchorY + STEP;
                        break;
                    case 7:
                        x = anchorX + 2 * STEP;
                        y = anchorY;
                        break;
                    case 8:
                        x = anchorX + STEP;
                        y = anchorY;
                        break;
                    case 9:
                        x = anchorX + STEP;
                        y = anchorY + STEP;
                        break;
                }
                path.lineTo(x, y);
            }
            mosaicPaint.setAlpha(155 + (int) (Math.random() * 100));

            canvas.drawPath(path, mosaicPaint);
            path.reset();

            Joint[] nextJoints = next.get(triangle);
            int index = (int) (Math.random() * nextJoints.length);
            if (index == nextJoints.length) {
                index--;
            }
            Joint joint = nextJoints[index];

            anchorX = anchorX + STEP * joint.shiftX;
            anchorY = anchorY + STEP * joint.shiftY;
            triangle = joint.right;

        }
        return bitmap;
    }

    /**
     * Triangle is described as series of points in matrix
     * 1 8 7
     * 2 9 6
     * 3 4 5
     **/
    class Triangle {
        final int[] points;

        Triangle(int... points) {
            this.points = points;
        }
    }

    class Joint {
        final Triangle right;
        final int shiftX;
        final int shiftY;

        Joint(Triangle right, int shiftX, int shiftY) {
            this.right = right;
            this.shiftX = shiftX;
            this.shiftY = shiftY;
        }
    }
}