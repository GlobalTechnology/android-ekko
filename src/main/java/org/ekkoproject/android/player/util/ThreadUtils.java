package org.ekkoproject.android.player.util;

import java.util.Map;

import android.os.Looper;

public final class ThreadUtils {
    public static void assertNotOnUiThread() {
        if (isUiThread()) {
            throw new RuntimeException("unsupported method on UI thread");
        }
    }

    public static void assertOnUiThread() {
        if (!isUiThread()) {
            throw new RuntimeException("method requires UI thread");
        }
    }

    public static <K> Object getLock(final Map<K, Object> locks, final K key) {
        synchronized (locks) {
            if (!locks.containsKey(key)) {
                locks.put(key, new Object());
            }
            return locks.get(key);
        }
    }

    public static boolean isUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
