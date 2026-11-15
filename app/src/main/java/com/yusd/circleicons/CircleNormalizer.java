package com.yusd.circleicons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import java.util.Arrays;

/** Promotes legacy artwork into Android's own adaptive-icon rendering pipeline. */
public final class CircleNormalizer {
    private static final int SIZE = 256;

    public static Drawable adapt(Drawable original) {
        if (original instanceof AdaptiveIconDrawable) return original;

        Bitmap image = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        int width = original.getIntrinsicWidth();
        int height = original.getIntrinsicHeight();
        float aspect = width > 0 && height > 0 ? (float) width / height : 1f;
        int drawWidth = aspect > 1 ? SIZE : Math.round(SIZE * aspect);
        int drawHeight = aspect > 1 ? Math.round(SIZE / aspect) : SIZE;
        DrawableRasterizer.draw(original, new Canvas(image),
                new Rect((SIZE - drawWidth) / 2, (SIZE - drawHeight) / 2,
                        (SIZE + drawWidth) / 2, (SIZE + drawHeight) / 2));

        int[] pixels = new int[SIZE * SIZE];
        image.getPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE);
        int left = SIZE, top = SIZE, right = 0, bottom = 0, opaque = 0;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int alpha = Color.alpha(pixels[y * SIZE + x]);
                if (alpha > 24) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x + 1);
                    bottom = Math.max(bottom, y + 1);
                }
                if (alpha > 240) opaque++;
            }
        }
        // An app is allowed to publish an entirely transparent icon.
        if (left == SIZE) {
            image.recycle();
            return original;
        }

        int cropWidth = right - left, cropHeight = bottom - top;
        float side = Math.max(cropWidth, cropHeight);
        float centerX = (left + right) / 2f, centerY = (top + bottom) / 2f;
        boolean plate = opaque / (float) (cropWidth * cropHeight) >= .70f;
        int background = plate ? perimeterColor(pixels, centerX, centerY, side) : Color.WHITE;
        if (plate) {
            erasePlateBackground(pixels, background);
            image.setPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE);
        }
        float radiusSquared = 0;
        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                if (Color.alpha(pixels[y * SIZE + x]) > 24) {
                    float dx = (x + .5f - centerX) / side;
                    float dy = (y + .5f - centerY) / side;
                    radiusSquared = Math.max(radiusSquared, dx * dx + dy * dy);
                }
            }
        }
        if (radiusSquared == 0) {
            image.recycle();
            return new AdaptiveIconDrawable(new ColorDrawable(background), new ColorDrawable(Color.TRANSPARENT));
        }
        // Fit the actual artwork into the mask; no brand-specific list or corner cropping.
        float scale = Math.min(.92f, .96f / (2f * (float) Math.sqrt(radiusSquared)));
        Bitmap cropped = Bitmap.createBitmap(image, left, top, cropWidth, cropHeight);
        if (cropped != image) image.recycle();
        BitmapDrawable foreground = new BitmapDrawable(Resources.getSystem(), cropped);
        float layerExpansion = 1f + 2f * AdaptiveIconDrawable.getExtraInsetFraction();
        float insetX = (1f - scale * cropWidth / side / layerExpansion) / 2f;
        float insetY = (1f - scale * cropHeight / side / layerExpansion) / 2f;
        return new AdaptiveIconDrawable(new ColorDrawable(background),
                new InsetDrawable(foreground, insetX, insetY, insetX, insetY));
    }

    private static void erasePlateBackground(int[] pixels, int background) {
        // Remove only matching colour connected to the outside. Enclosed artwork stays intact.
        boolean[] visited = new boolean[pixels.length];
        int[] queue = new int[pixels.length];
        int tail = 0;
        for (int i = 0; i < pixels.length; i++) {
            int x = i % SIZE, y = i / SIZE;
            if (x == 0 || x == SIZE - 1 || y == 0 || y == SIZE - 1) {
                queue[tail++] = i;
                visited[i] = true;
            }
        }
        for (int head = 0; head < tail; head++) {
            int i = queue[head], color = pixels[i];
            int red = Color.red(color) - Color.red(background);
            int green = Color.green(color) - Color.green(background);
            int blue = Color.blue(color) - Color.blue(background);
            if (Color.alpha(color) > 24 && red * red + green * green + blue * blue > 6400) continue;
            pixels[i] = Color.TRANSPARENT;
            int x = i % SIZE, y = i / SIZE;
            if (x > 0 && !visited[i - 1]) { visited[i - 1] = true; queue[tail++] = i - 1; }
            if (x < SIZE - 1 && !visited[i + 1]) { visited[i + 1] = true; queue[tail++] = i + 1; }
            if (y > 0 && !visited[i - SIZE]) { visited[i - SIZE] = true; queue[tail++] = i - SIZE; }
            if (y < SIZE - 1 && !visited[i + SIZE]) { visited[i + SIZE] = true; queue[tail++] = i + SIZE; }
        }
    }

    private static int perimeterColor(int[] pixels, float cx, float cy, float side) {
        int[] reds = new int[48], greens = new int[48], blues = new int[48];
        int count = 0;
        for (int ray = 0; ray < 48; ray++) {
            double angle = ray * Math.PI * 2 / 48;
            float dx = (float) Math.cos(angle), dy = (float) Math.sin(angle);
            for (int distance = (int) side; distance >= 0; distance--) {
                int x = Math.round(cx + dx * distance), y = Math.round(cy + dy * distance);
                if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) continue;
                int color = pixels[y * SIZE + x];
                if (Color.alpha(color) <= 240) continue;
                int insideX = Math.round(cx + dx * Math.max(0, distance - 3));
                int insideY = Math.round(cy + dy * Math.max(0, distance - 3));
                color = pixels[insideY * SIZE + insideX];
                reds[count] = Color.red(color);
                greens[count] = Color.green(color);
                blues[count] = Color.blue(color);
                count++;
                break;
            }
        }
        Arrays.sort(reds, 0, count);
        Arrays.sort(greens, 0, count);
        Arrays.sort(blues, 0, count);
        return Color.rgb(reds[count / 2], greens[count / 2], blues[count / 2]);
    }
}
