package com.quewelcy.omnios.view.pattern;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

public class PaperStack extends WallPattern {

    private float angleStart;
    private float angleAdd;
    private int count;

    public PaperStack(int width, int height) {
        super(width, height);
        angleStart = (float) (Math.random() * 30);
        angleAdd = (float) (Math.random() * 30);
        count = (int) (3 + 4 * Math.random());
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

        double x = 0;
        double y = 0;

        for (int i = 0; i < count; i++) {
            paint.setColor(getNextColor());

            Rect rect = new Rect();
            rect.set((int) x, (int) y, (int) (x + width), (int) (y + height));
            canvas.drawRect(rect, paint);
            canvas.rotate(i == 0 ? angleStart : angleAdd,
                    (float) x, (float) y);
        }
        return bitmap;
    }
}