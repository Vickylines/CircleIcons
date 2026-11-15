package com.yusd.circleicons;

import static org.junit.Assert.*;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LevelListDrawable;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Test;

/** Pixel assertions on Android Canvas, not mocks of the normalization algorithm. */
public final class IconRenderingTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final Resources resources = context.getResources();

    private BitmapDrawable plate(int background, int foreground, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(background);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(foreground);
        canvas.drawRect(width * .4f, height * .16f, width * .6f, height * .84f, paint);
        canvas.drawRect(width * .16f, height * .4f, width * .84f, height * .6f, paint);
        return new BitmapDrawable(resources, bitmap);
    }
    private Bitmap render(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
        DrawableRasterizer.draw(drawable, new Canvas(bitmap), new Rect(0, 0, 96, 96));
        return bitmap;
    }
    private int occupied(Bitmap bitmap) {
        int count = 0;
        for (int y=0; y<bitmap.getHeight(); y++) for (int x=0; x<bitmap.getWidth(); x++) {
            if (Color.alpha(bitmap.getPixel(x,y)) > 24) count++;
        }
        return count;
    }
    @Test public void nativeAdaptiveIdentityAndBoundsSurvive() {
        AdaptiveIconDrawable adaptive = new AdaptiveIconDrawable(new ColorDrawable(Color.BLUE), new ColorDrawable(Color.YELLOW));
        adaptive.setBounds(7, 11, 130, 145);
        assertSame(adaptive, CircleNormalizer.adapt(adaptive));
        BitmapDrawable legacy = plate(Color.RED, Color.WHITE, 128, 128);
        Rect before = new Rect(7, 9, 87, 105);
        legacy.setBounds(before);
        Drawable adapted = CircleNormalizer.adapt(legacy);
        assertTrue(adapted instanceof AdaptiveIconDrawable);
        assertEquals(before, legacy.getBounds());
        Bitmap actual = render(adapted);
        Bitmap expectedMask = render(new AdaptiveIconDrawable(new ColorDrawable(Color.RED), new ColorDrawable(Color.TRANSPARENT)));
        for (int y=0; y<96; y++) for (int x=0; x<96; x++) {
            assertEquals(Color.alpha(expectedMask.getPixel(x,y)), Color.alpha(actual.getPixel(x,y)));
        }
        assertEquals(Color.WHITE, actual.getPixel(48,48));
    }
    @Test public void notificationGlyphRetainsAlphaUnderBothSystemTints() throws IOException {
        for (int[] colors : new int[][]{{Color.RED, Color.WHITE}, {Color.WHITE, Color.BLACK}, {Color.BLACK, Color.WHITE}}) {
            Drawable source = plate(colors[0], colors[1], 128, 128);
            Drawable glyph = StatusIconNormalizer.adapt(source, resources);
            assertNotSame(source, glyph);
            glyph.setTint(Color.WHITE);
            Bitmap onDark = render(glyph);
            glyph.setTint(Color.BLACK);
            Bitmap onLight = render(glyph);
            int occupied = occupied(onDark);
            assertTrue("Glyph must remain visible", occupied > 400);
            assertTrue("Opaque background must be removed", occupied < 96*96*.55f);
            for (int y=0; y<96; y++) for (int x=0; x<96; x++) {
                int white=onDark.getPixel(x,y), black=onLight.getPixel(x,y);
                assertEquals(Color.alpha(white), Color.alpha(black));
                if (Color.alpha(white) > 240) {
                    // Premultiplied alpha can round a white anti-aliased edge to RGB 254.
                    assertTrue(Color.red(white) >= 254 && Color.green(white) >= 254 && Color.blue(white) >= 254);
                    assertEquals(0, black & 0x00ffffff);
                }
            }
        }
        exportPreview();
    }

    private void exportPreview() throws IOException {
        Bitmap board = Bitmap.createBitmap(960, 540, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(board);
        canvas.drawColor(Color.rgb(237, 241, 247));
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(35, 46, 65));
        paint.setTextSize(34);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif-medium", 0));
        canvas.drawText("CircleIcons", 48, 58, paint);
        paint.setTextSize(20);
        String[] labels = {"Original artwork", "System mask", "Dark background", "Light background"};
        for (int col = 0; col < 4; col++) canvas.drawText(labels[col], 48 + col * 224, 104, paint);
        int[][] colors = {{Color.rgb(216, 45, 55), Color.WHITE}, {Color.WHITE, Color.BLACK}, {Color.BLACK, Color.WHITE}};
        for (int row = 0; row < colors.length; row++) {
            int y = 140 + row * 124;
            Drawable original = plate(colors[row][0], colors[row][1], 128, 128);
            DrawableRasterizer.draw(original, canvas, new Rect(88, y, 176, y + 88));
            DrawableRasterizer.draw(CircleNormalizer.adapt(original), canvas, new Rect(312, y, 400, y + 88));
            for (int col = 2; col < 4; col++) {
                int x = 48 + col * 224;
                paint.setColor(col == 2 ? Color.rgb(32, 39, 52) : Color.WHITE);
                canvas.drawRoundRect(x, y - 8, x + 174, y + 98, 18, 18, paint);
                Drawable glyph = StatusIconNormalizer.adapt(original, resources);
                glyph.setTint(col == 2 ? Color.WHITE : Color.BLACK);
                DrawableRasterizer.draw(glyph, canvas, new Rect(x + 67, y + 24, x + 107, y + 64));
            }
        }
        try (FileOutputStream stream = new FileOutputStream(new File(context.getFilesDir(), "preview.png"))) {
            assertTrue(board.compress(Bitmap.CompressFormat.PNG, 100, stream));
        }
    }
    @Test public void existingGlyphEmptyAndSolidArtworkRemainUntouched() {
        Drawable glyph = plate(Color.TRANSPARENT, Color.WHITE, 128, 128);
        assertSame(glyph, StatusIconNormalizer.adapt(glyph, resources));
        Drawable empty = new ColorDrawable(Color.TRANSPARENT);
        assertSame(empty, StatusIconNormalizer.adapt(empty, resources));
        assertSame(empty, CircleNormalizer.adapt(empty));
        Drawable solid = new ColorDrawable(Color.WHITE);
        assertSame(solid, StatusIconNormalizer.adapt(solid, resources));
    }
    @Test public void dynamicNotificationArtworkIsPreserved() {
        AnimationDrawable animation = new AnimationDrawable();
        animation.addFrame(new ColorDrawable(Color.RED), 100);
        animation.addFrame(new ColorDrawable(Color.BLUE), 100);
        Drawable wrapped = new InsetDrawable(animation, 0);
        assertSame(wrapped, StatusIconNormalizer.adapt(wrapped, resources));
        LevelListDrawable levels = new LevelListDrawable();
        levels.addLevel(0, 10, plate(Color.RED, Color.WHITE, 64, 64));
        assertSame(levels, StatusIconNormalizer.adapt(levels, resources));
    }
    @Test public void softwareNormalizationAcceptsHardwareBitmaps() {
        BitmapDrawable source = plate(Color.RED, Color.WHITE, 128, 128);
        Bitmap hardware = source.getBitmap().copy(Bitmap.Config.HARDWARE, false);
        assertNotNull(hardware);
        Drawable drawable = new BitmapDrawable(resources, hardware);
        assertTrue(CircleNormalizer.adapt(drawable) instanceof AdaptiveIconDrawable);
        assertNotSame(drawable, StatusIconNormalizer.adapt(drawable, resources));
    }
    @Test public void largeNotificationResourcesHaveBoundedOutput() {
        Drawable source = plate(Color.RED, Color.WHITE, 1024, 512);
        Drawable glyph = StatusIconNormalizer.adapt(source, resources);
        assertTrue(glyph.getIntrinsicWidth() <= 256);
        assertTrue(glyph.getIntrinsicHeight() <= 256);
        assertEquals(2f, (float) glyph.getIntrinsicWidth()/glyph.getIntrinsicHeight(), .02f);
    }
}
