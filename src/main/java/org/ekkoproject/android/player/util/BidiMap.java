package org.ekkoproject.android.player.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BidiMap<K, V> implements Map<K, V> {
    private final HashMap<K, V> map;
    private final HashMap<V, K> reverseMap;

    private final Map<K, V> lockedMap;
    private final Map<V, K> lockedReverseMap;

    public BidiMap() {
        this(0);
    }

    public BidiMap(final int capacity) {
        this.map = new HashMap<K, V>(capacity);
        this.reverseMap = new HashMap<V, K>(capacity);
        this.lockedMap = Collections.unmodifiableMap(this.map);
        this.lockedReverseMap = Collections.unmodifiableMap(this.reverseMap);
    }

    public BidiMap(Map<? extends K, ? extends V> map) {
        this(map != null ? map.size() : 0);
        this.putAll(map);
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.reverseMap.containsKey(value);
    }

    @Override
    public V put(final K key, final V value) {
        // remove previous entries
        final V previousValue = this.remove(key);
        this.removeValue(value);

        // store new value
        this.map.put(key, value);
        this.reverseMap.put(value, key);

        // return previous value
        return previousValue;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(final Object key) {
        final V removed = this.map.remove(key);
        this.reverseMap.remove(removed);
        return removed;
    }

    public K removeValue(final Object value) {
        final K removed = this.reverseMap.remove(value);
        this.map.remove(removed);
        return removed;
    }

    @Override
    public Collection<V> values() {
        return this.lockedReverseMap.keySet();
    }

    @Override
    public void clear() {
        this.map.clear();
        this.reverseMap.clear();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.lockedMap.entrySet();
    }

    @Override
    public V get(final Object key) {
        return this.map.get(key);
    }

    public K getKey(final Object value) {
        return this.reverseMap.get(value);
    }

    @Override
    public boolean isEmpty() {
        return this.map.size() <= 0;
    }

    @Override
    public Set<K> keySet() {
        return this.lockedMap.keySet();
    }

    @Override
    public int size() {
        return this.map.size();
    }
}
