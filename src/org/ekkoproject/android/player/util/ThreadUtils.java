package org.ekkoproject.android.player.util;

import android.os.Looper;

public final class ThreadUtils {
    public static void assertNotOnUiThread() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new RuntimeException("unsupported method on UI thread");
        }
    }

    public static void assertOnUiThread() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new RuntimeException("method requires UI thread");
        }
    }
}
