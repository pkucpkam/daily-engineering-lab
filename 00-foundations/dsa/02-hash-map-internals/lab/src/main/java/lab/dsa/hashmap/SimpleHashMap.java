package lab.dsa.hashmap;

import java.util.Objects;

/**
 * Tự cài đặt HashMap tối giản để học trực tiếp cơ chế bên trong:
 * - Capacity luôn là lũy thừa của 2 (mặc định 16), loadFactor = 0.75.
 * - Perturbation hash: (h = key.hashCode()) ^ (h >>> 16).
 * - Bitmask index: (capacity - 1) & hash.
 * - Resize 2x tách nhánh nhanh bằng bit (e.hash & oldCap).
 */
public final class SimpleHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    static final class Node<K, V> {
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
    }

    private Node<K, V>[] table;
    private int size;
    private int threshold;
    private int resizeCount;

    @SuppressWarnings("unchecked")
    public SimpleHashMap(int initialCapacity) {
        int cap = tableSizeFor(Math.max(1, initialCapacity));
        this.table = (Node<K, V>[]) new Node[cap];
        this.threshold = (int) (cap * LOAD_FACTOR);
    }

    public SimpleHashMap() {
        this(DEFAULT_CAPACITY);
    }

    static int hash(Object key) {
        if (key == null) {
            return 0;
        }
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    static int tableSizeFor(int cap) {
        int n = Integer.highestOneBit(cap - 1) << 1;
        return Math.max(16, Math.min(n, 1 << 30));
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return table.length;
    }

    public int resizeCount() {
        return resizeCount;
    }

    public V put(K key, V value) {
        int h = hash(key);
        int idx = (table.length - 1) & h;
        Node<K, V> head = table[idx];

        if (head == null) {
            table[idx] = new Node<>(h, key, value, null);
        } else {
            Node<K, V> curr = head;
            Node<K, V> tail = null;
            while (curr != null) {
                if (curr.hash == h && Objects.equals(curr.key, key)) {
                    V old = curr.value;
                    curr.value = value;
                    return old;
                }
                tail = curr;
                curr = curr.next;
            }
            // Tail insertion giống Java 8+
            tail.next = new Node<>(h, key, value, null);
        }

        if (++size > threshold) {
            resize();
        }
        return null;
    }

    public V get(Object key) {
        int h = hash(key);
        int idx = (table.length - 1) & h;
        for (Node<K, V> e = table[idx]; e != null; e = e.next) {
            if (e.hash == h && Objects.equals(e.key, key)) {
                return e.value;
            }
        }
        return null;
    }

    public V remove(Object key) {
        int h = hash(key);
        int idx = (table.length - 1) & h;
        Node<K, V> prev = null;
        Node<K, V> curr = table[idx];
        while (curr != null) {
            if (curr.hash == h && Objects.equals(curr.key, key)) {
                if (prev == null) {
                    table[idx] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return curr.value;
            }
            prev = curr;
            curr = curr.next;
        }
        return null;
    }

    public int maxBucketChainLength() {
        int max = 0;
        for (Node<K, V> head : table) {
            int len = 0;
            for (Node<K, V> e = head; e != null; e = e.next) {
                len++;
            }
            if (len > max) {
                max = len;
            }
        }
        return max;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = oldTab.length;
        int newCap = oldCap << 1;
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        threshold = (int) (newCap * LOAD_FACTOR);

        for (int j = 0; j < oldCap; j++) {
            Node<K, V> e = oldTab[j];
            if (e == null) {
                continue;
            }
            Node<K, V> loHead = null, loTail = null;
            Node<K, V> hiHead = null, hiTail = null;
            while (e != null) {
                Node<K, V> next = e.next;
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
            }
            if (loTail != null) {
                loTail.next = null;
                newTab[j] = loHead;
            }
            if (hiTail != null) {
                hiTail.next = null;
                newTab[j + oldCap] = hiHead;
            }
        }
        table = newTab;
        resizeCount++;
    }
}
