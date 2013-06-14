package org.ekkoproject.android.player.util;

import static org.ekkoproject.android.player.Constants.DEFAULT_LAYOUT;

public final class ViewUtils {
    public static void assertValidLayout(final int layout) {
        if (layout == DEFAULT_LAYOUT) {
            throw new RuntimeException("invalid layout specified");
        }
    }
}
