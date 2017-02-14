package com.quewelcy.omnios.view.mono;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

public abstract class WhiteIcon {

    protected int width;
    protected int height;
    protected int color = 0xAAFFFFFF;

    WhiteIcon(int size) {
        this(size, size);
    }

    private WhiteIcon(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Drawable getDrawable(Resources resources) {
        return new BitmapDrawable(resources, getBitmap());
    }

    private Bitmap getBitmap() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        draw(new Canvas(bitmap));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return bitmap;
    }

    protected abstract void draw(Canvas canvas);
}
