package com.yusd.circleicons;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

final class DrawableRasterizer {
    static void draw(Drawable original, Canvas canvas, Rect target) {
        // Icon.loadDrawable may return a hardware bitmap even though normalization uses a software canvas.
        if (original instanceof BitmapDrawable
                && ((BitmapDrawable) original).getBitmap().getConfig() == Bitmap.Config.HARDWARE) {
            BitmapDrawable bitmap = (BitmapDrawable) original;
            Bitmap software = bitmap.getBitmap().copy(Bitmap.Config.ARGB_8888, false);
            canvas.drawBitmap(software, null, target, bitmap.getPaint());
            software.recycle();
            return;
        }
        Rect previous = new Rect(original.getBounds());
        try {
            original.setBounds(target);
            original.draw(canvas);
        } finally {
            original.setBounds(previous);
        }
    }
}
