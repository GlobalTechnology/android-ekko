package org.ekkoproject.android.player.util;

import static org.ekkoproject.android.player.Constants.DEFAULT_LAYOUT;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public final class ViewUtils {
    public static void assertValidLayout(final int layout) {
        if (layout == DEFAULT_LAYOUT) {
            throw new RuntimeException("invalid layout specified");
        }
    }

    public static Bitmap getBitmapFromView(final View view) {
        if (view != null) {
            final int width = view.getWidth();
            final int height = view.getHeight();
            if (width > 0 && height > 0) {
                final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);
                return bitmap;
            }
        }

        return null;
    }
}
