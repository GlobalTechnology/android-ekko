package org.ekkoproject.android.player.util;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.support.v4.util.LruCache;

public class WeakLruCache<K, V> extends LruCache<K, V> {
    final Map<K, WeakReference<V>> backup = new HashMap<K, WeakReference<V>>();

    public WeakLruCache(final int maxSize) {
        super(maxSize);
    }

    @Override
    protected void entryRemoved(final boolean evicted, final K key, final V oldValue, final V newValue) {
        if (evicted) {
            this.backup.put(key, new WeakReference<V>(oldValue));
        }
    }

    @Override
    protected V create(final K key) {
        final WeakReference<V> ref = this.backup.remove(key);
        if (ref != null) {
            return ref.get();
        }

        return super.create(key);
    }
}
