package com.quewelcy.omnios.view.pattern;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

public class CrossRopes extends WallPattern {

    private final float angleStart;
    private final float angleAdd;
    private final int count;

    public CrossRopes(int size) {
        super(size);
        angleStart = (float) (Math.random() * 90);
        angleAdd = (float) (Math.random() * 60);
        count = (int) (2 + 3 * Math.random());
    }

    @Override
    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        paint.setDither(true);

        canvas.drawColor(chooseSchemeAndGetColor());

        double x = Math.random() * width * 2 / 3;
        double y = Math.random() * height * 2 / 3;

        for (int i = 0; i < count; i++) {
            paint.setColor(getNextColor());

            Rect rect = new Rect();
            rect.set((int) x, -height, (int) (x + width / 10), 2 * height);
            canvas.drawRect(rect, paint);
            canvas.rotate(i == 0 ? angleStart : angleAdd,
                    (float) x, (float) y);
        }
        return bitmap;
    }
}