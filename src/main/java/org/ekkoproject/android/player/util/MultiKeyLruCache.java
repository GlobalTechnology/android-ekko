package org.ekkoproject.android.player.util;

import android.support.v4.util.LruCache;

import java.util.HashMap;
import java.util.Map;

public class MultiKeyLruCache<K, V> extends LruCache<K, V> {
    private int sizeGap = 0;

    private final Map<V, Integer> copies = new HashMap<>();

    public MultiKeyLruCache(final int maxSize) {
        super(maxSize);
    }

    public V putMulti(final K key, final V value) {
        incCount(key, value);
        return this.put(key, value);
    }

    @Override
    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
        decCount(key, oldValue);
        super.entryRemoved(evicted, key, oldValue, newValue);
    }

    @Override
    protected final V create(final K key) {
        final V value = this.createMulti(key);
        if (value != null) {
            incCount(key, value);
        }
        return value;
    }

    protected V createMulti(final K key) {
        return null;
    }

    @Override
    public void trimToSize(final int maxSize) {
        super.trimToSize(maxSize + sizeGap);
    }

    private void incCount(final K key, final V value) {
        Integer count;
        synchronized (this.copies) {
            count = this.copies.get(value);
            count = count != null ? count + 1 : 1;
            this.copies.put(value, count);
        }
        if (count > 1) {
            final int size = this.sizeOf(key, value);
            if (size >= 0) {
                sizeGap += size;
            }
        }
    }

    private void decCount(final K key, final V value) {
        Integer count;
        synchronized (this.copies) {
            count = this.copies.get(value);
            count = count != null ? count - 1 : 0;
            if (count <= 0) {
                this.copies.remove(value);
            } else {
                this.copies.put(value, count);
            }
        }
        if (count > 0) {
            final int size = this.sizeOf(key, value);
            if (size >= 0) {
                sizeGap -= size < sizeGap ? size : sizeGap;
            }
        }
    }
}
