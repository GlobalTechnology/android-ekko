package org.ekkoproject.android.player.util;

import static org.ekkoproject.android.player.Constants.DEFAULT_LAYOUT;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.view.View;

public final class ViewUtils {
    public static void assertValidLayout(final int layout) {
        if (layout == DEFAULT_LAYOUT) {
            throw new RuntimeException("invalid layout specified");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void fragmentAnimationHack(final Fragment fragment) {
        final View view = fragment.getView();
        if (view != null) {
            final BitmapDrawable background = new BitmapDrawable(fragment.getResources(), loadBitmapFromView(view));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackground(background);
            } else {
                view.setBackgroundDrawable(background);
            }
        }
    }

    public static Bitmap loadBitmapFromView(final View view) {
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getWidth(), view.getHeight());
        view.draw(canvas);
        return bitmap;
    }
}
