package org.ekkoproject.android.player.util;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class WeakMultiKeyLruCache<K, V> extends MultiKeyLruCache<K, V> {
    final Map<K, WeakReference<V>> backup = new HashMap<K, WeakReference<V>>();

    public WeakMultiKeyLruCache(final int maxSize) {
        super(maxSize);
    }

    @Override
    protected void entryRemoved(final boolean evicted, final K key, final V oldValue, final V newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
        if (evicted) {
            this.backup.put(key, new WeakReference<V>(oldValue));
        }
    }

    @Override
    protected V createMulti(final K key) {
        final WeakReference<V> ref = this.backup.remove(key);
        if (ref != null) {
            return ref.get();
        }
        return super.createMulti(key);
    }
}
