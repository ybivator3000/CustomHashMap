package ru.homework.CustomHashMap.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Свои реализации абстрактных классов и интерфейсов наверное лучше не делать.
 * Это будет излишком, а вот подсмотреть в HashMap и унаследоваться от уже
 * реализованных классов и интерфейсов, какбуд-то верное решение.
 * Реализовать надо только базовый набор методов(ибо я посмотрел и все остальное очень страшно)
 * обязательно впихнуть итератор
 *
 * @param <K>
 * @param <V>
 */
public class CustomHashMap<K, V> extends AbstractMap<K, V> {
    private static final int DEFAULT_CAPACITY = 1 << 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public final K getKey() {
            return key;
        }

        @Override
        public final V getValue() {
            return value;
        }

        @Override
        public final String toString() {
            return key + "=" + value;
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public final V setValue(V newValue) {
            V old = value;
            value = newValue;
            return old;
        }

        @Override
        public final boolean equals(Object o) {
            if (o == this) return true;
            return o instanceof Map.Entry<?, ?> e && Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
        }
    }

    private Node<K, V>[] table;
    private int size;
    private int threshold;
    private final float loadFactor;
    private int modCount;

    private Set<K> keySet;
    private Collection<V> values;
    private Set<Map.Entry<K, V>> entrySet;

    public CustomHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    public CustomHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public CustomHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

    public CustomHashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putAll(m);
    }

    static int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    static int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    private Node<K, V> getNode(Object key) {
        Node<K, V>[] tab = table;
        if (tab == null || size == 0) return null;

        int hash = hash(key);
        int index = (tab.length - 1) & hash;
        Node<K, V> p = tab[index];

        while (p != null) {
            if (p.hash == hash && (p.key == key || (key != null && key.equals(p.key)))) {
                return p;
            }
            p = p.next;
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return getNode(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Node<K, V>[] tab = table;
        if (tab != null && size > 0) {
            for (Node<K, V> head : tab) {
                for (Node<K, V> p = head; p != null; p = p.next) {
                    if (Objects.equals(value, p.value)) return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        Node<K, V> e = getNode(key);
        return (e == null) ? null : e.value;
    }

    @Override
    public V put(K key, V value) {
        if (table == null) {
            resize();
        }

        int hash = hash(key);
        int index = (table.length - 1) & hash;

        Node<K, V> p = table[index];
        while (p != null) {
            if (p.hash == hash && (p.key == key || (key != null && key.equals(p.key)))) {
                V old = p.value;
                p.value = value;
                return old;
            }
            p = p.next;
        }


        table[index] = new Node<>(hash, key, value, table[index]);
        modCount++;
        if (++size > threshold) {
            resize();
        }
        return null;
    }

    @Override
    public V remove(Object key) {
        Node<K, V>[] tab = table;
        if (tab == null || size == 0) return null;

        int hash = hash(key);
        int index = (tab.length - 1) & hash;

        Node<K, V> prev = null;
        Node<K, V> p = tab[index];

        while (p != null) {
            if (p.hash == hash && (p.key == key || (key != null && key.equals(p.key)))) {
                if (prev == null) {
                    tab[index] = p.next;
                } else {
                    prev.next = p.next;
                }
                size--;
                modCount++;
                return p.value;
            }
            prev = p;
            p = p.next;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }


    @Override
    public void clear() {
        if (table != null && size > 0) {
            Arrays.fill(table, null);
            size = 0;
            modCount++;
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTable = table;
        int oldCap = (oldTable == null) ? 0 : oldTable.length;
        int oldThr = threshold;
        int newCap;
        int newThr = 0;

        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return;
            }
            newCap = oldCap << 1;
            if (newCap < MAXIMUM_CAPACITY && oldCap >= DEFAULT_CAPACITY) {
                newThr = oldThr << 1;
            }
        } else if (oldThr > 0) {
            newCap = oldThr;
        } else {
            newCap = DEFAULT_CAPACITY;
        }

        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY) ? (int) ft : Integer.MAX_VALUE;
        }
        threshold = newThr;

        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCap];

        if (oldTable != null) {
            for (int i = 0; i < oldCap; i++) {
                Node<K, V> e = oldTable[i];
                if (e == null) continue;
                oldTable[i] = null;

                if (e.next == null) {
                    newTable[(newCap - 1) & e.hash] = e;
                } else {
                    Node<K, V> loHead = null, loTail = null;
                    Node<K, V> hiHead = null, hiTail = null;
                    Node<K, V> next;
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null) loHead = e;
                            else loTail.next = e;
                            loTail = e;
                        } else {
                            if (hiTail == null) hiHead = e;
                            else hiTail.next = e;
                            hiTail = e;
                        }
                        e = next;
                    } while (e != null);

                    if (loTail != null) {
                        loTail.next = null;
                        newTable[i] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTable[i + oldCap] = hiHead;
                    }
                }
            }
        }
        table = newTable;
    }

    private abstract class HashIterator {
        Node<K, V> next;
        Node<K, V> current;
        int expectedModCount;
        int index;

        HashIterator() {
            expectedModCount = modCount;
            Node<K, V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) {
                while (index < t.length && (next = t[index]) == null) {
                    index++;
                }
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K, V> nextNode() {
            Node<K, V>[] t;
            Node<K, V> e = next;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (e == null) {
                throw new NoSuchElementException();
            }
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {

                } while (++index < t.length && (next = t[index]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K, V> p = current;
            if (p == null) {
                throw new IllegalStateException();
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            current = null;
            K key = p.key;
            CustomHashMap.this.remove(key);
            expectedModCount = modCount;
        }
    }

    private final class KeyIterator extends HashIterator implements Iterator<K> {
        @Override
        public K next() {
            return nextNode().key;
        }
    }

    private final class ValueIterator extends HashIterator implements Iterator<V> {
        @Override
        public V next() {
            return nextNode().value;
        }
    }

    private final class EntryIterator extends HashIterator implements Iterator<Map.Entry<K, V>> {
        @Override
        public Map.Entry<K, V> next() {
            return nextNode();
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    private final class KeySet extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            if (!containsKey(o)) {
                return false;
            }
            CustomHashMap.this.remove(o);
            return true;
        }

        @Override
        public void clear() {
            CustomHashMap.this.clear();
        }
    }

    @Override
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    private final class Values extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public boolean contains(Object o) {
            return containsValue(o);
        }

        @Override
        public void clear() {
            CustomHashMap.this.clear();
        }
    }

    @Override
    @NotNull
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        if (es == null) {
            es = new EntrySet();
            entrySet = es;
        }
        return es;
    }

    private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public void clear() {
            CustomHashMap.this.clear();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry<?, ?> e)) return false;
            Node<K, V> candidate = getNode(e.getKey());
            return candidate != null && Objects.equals(candidate.value, e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry<?, ?> e)) return false;
            Node<K, V> candidate = getNode(e.getKey());
            if (candidate == null || !Objects.equals(candidate.value, e.getValue())) {
                return false;
            }
            CustomHashMap.this.remove(e.getKey());
            return true;
        }
    }
}