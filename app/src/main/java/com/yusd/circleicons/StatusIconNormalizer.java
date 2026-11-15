package com.yusd.circleicons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;

/** Repairs opaque artwork; SystemUI still owns the wallpaper/light/dark tint. */
public final class StatusIconNormalizer {
    private static final int SIZE = 128;

    public static Drawable adapt(Drawable original, Resources resources) {
        if (isDynamic(original)) return original;
        Bitmap flat = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        DrawableRasterizer.draw(original, new Canvas(flat), new Rect(0, 0, SIZE, SIZE));
        int[] pixels = new int[SIZE * SIZE];
        flat.getPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE);
        int left = SIZE, top = SIZE, right = 0, bottom = 0, opaque = 0;
        int darkest = 765, lightest = 0;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int color = pixels[y * SIZE + x];
                if (Color.alpha(color) > 24) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x + 1);
                    bottom = Math.max(bottom, y + 1);
                }
                if (Color.alpha(color) > 240) {
                    opaque++;
                    int brightness = Color.red(color) + Color.green(color) + Color.blue(color);
                    darkest = Math.min(darkest, brightness);
                    lightest = Math.max(lightest, brightness);
                }
            }
        }
        // Correct small icons already carry their shape in alpha, or use one solid colour.
        if (left == SIZE || opaque < (right - left) * (bottom - top) * .70f
                || lightest - darkest < 60) {
            flat.recycle();
            return original;
        }
        AdaptiveIconDrawable adaptive = (AdaptiveIconDrawable) CircleNormalizer.adapt(
                new BitmapDrawable(resources, flat));
        Drawable mono = new MonochromeIconFactory(96).wrap(adaptive);
        int size = Math.round(24 * resources.getDisplayMetrics().density);
        int width = original.getIntrinsicWidth() > 0 ? original.getIntrinsicWidth() : size;
        int height = original.getIntrinsicHeight() > 0 ? original.getIntrinsicHeight() : size;
        float scale = Math.min(1f, 256f / Math.max(width, height));
        width = Math.max(1, Math.round(width * scale));
        height = Math.max(1, Math.round(height * scale));
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setDensity(resources.getDisplayMetrics().densityDpi);
        mono.setBounds(0, 0, width, height);
        mono.draw(new Canvas(result));
        flat.recycle();
        return new BitmapDrawable(resources, result);
    }
    private static boolean isDynamic(Drawable drawable) {
        if (drawable instanceof Animatable || drawable instanceof LevelListDrawable) return true;
        return drawable instanceof android.graphics.drawable.DrawableWrapper
                && isDynamic(((android.graphics.drawable.DrawableWrapper) drawable).getDrawable());
    }
}
