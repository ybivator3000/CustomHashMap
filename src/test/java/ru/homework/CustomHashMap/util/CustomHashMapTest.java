package ru.homework.CustomHashMap.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CustomHashMapTest {

    private CustomHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new CustomHashMap<>();
    }


    @Test
    void hash() {

        assertEquals(0, CustomHashMap.hash(null));
        assertEquals(CustomHashMap.hash("key"), CustomHashMap.hash("key"));

        int h = CustomHashMap.hash("hello");
        assertEquals(h, CustomHashMap.hash(new String("hello")));


        int raw = "abc".hashCode();
        assertEquals(raw ^ (raw >>> 16), CustomHashMap.hash("abc"));


        assertNotEquals(CustomHashMap.hash("a"), CustomHashMap.hash("b"));


        assertDoesNotThrow(() -> CustomHashMap.hash(null));
    }


    @Test
    void tableSizeFor() {

        assertEquals(1, CustomHashMap.tableSizeFor(0));
        assertEquals(1, CustomHashMap.tableSizeFor(-5));
        assertEquals(1, CustomHashMap.tableSizeFor(1));


        assertEquals(2, CustomHashMap.tableSizeFor(2));
        assertEquals(4, CustomHashMap.tableSizeFor(4));
        assertEquals(16, CustomHashMap.tableSizeFor(16));
        assertEquals(64, CustomHashMap.tableSizeFor(64));

        assertEquals(2, CustomHashMap.tableSizeFor(2));
        assertEquals(4, CustomHashMap.tableSizeFor(3));
        assertEquals(8, CustomHashMap.tableSizeFor(5));
        assertEquals(16, CustomHashMap.tableSizeFor(9));
        assertEquals(32, CustomHashMap.tableSizeFor(17));
        assertEquals(1024, CustomHashMap.tableSizeFor(1000));
        int max = 1 << 30;
        assertEquals(max, CustomHashMap.tableSizeFor(max));
        assertEquals(max, CustomHashMap.tableSizeFor(Integer.MAX_VALUE));
    }


    @Test
    void size() {
        assertEquals(0, map.size());

        map.put("a", 1);
        assertEquals(1, map.size());

        map.put("b", 2);
        map.put("c", 3);
        assertEquals(3, map.size());

        map.put("a", 100);
        assertEquals(3, map.size());

        map.put(null, 42);
        assertEquals(4, map.size());

        map.remove("b");
        assertEquals(3, map.size());
    }

    @Test
    void isEmpty() {
        assertTrue(map.isEmpty());

        map.put("k", 1);
        assertFalse(map.isEmpty());

        map.remove("k");
        assertTrue(map.isEmpty());


        map.put("k", 1);
        map.clear();
        assertTrue(map.isEmpty());
    }


    @Test
    void containsKey() {
        assertFalse(map.containsKey("a"));

        map.put("a", 1);
        map.put("b", null);
        map.put(null, 99);

        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey(null));

        assertFalse(map.containsKey("c"));
        assertFalse(map.containsKey(123));


        map.remove("a");
        assertFalse(map.containsKey("a"));
    }

    @Test
    void containsValue() {
        assertFalse(map.containsValue(1));

        map.put("a", 1);
        map.put("b", 2);
        map.put("c", null);

        assertTrue(map.containsValue(1));
        assertTrue(map.containsValue(2));
        assertTrue(map.containsValue(null));
        assertFalse(map.containsValue(999));

        map.put("d", 1);
        assertTrue(map.containsValue(1));
    }


    @Test
    void get() {
        assertNull(map.get("missing"));

        map.put("a", 1);
        map.put("b", null);
        map.put(null, 10);

        assertEquals(1, map.get("a"));
        assertNull(map.get("b"));
        assertEquals(10, map.get(null));
        assertNull(map.get("zzz"));

        assertTrue(map.containsKey("b"));
    }

    @Test
    void put() {
        assertNull(map.put("a", 1));
        assertEquals(Integer.valueOf(1), map.put("a", 2));
        assertEquals(Integer.valueOf(2), map.get("a"));

        assertNull(map.put("n", null));
        assertTrue(map.containsKey("n"));
        assertNull(map.get("n"));


        assertNull(map.put(null, 100));
        assertEquals(Integer.valueOf(100), map.put(null, 200));
        assertEquals(Integer.valueOf(200), map.get(null));

        for (int i = 0; i < 1000; i++) {
            map.put("key" + i, i);
        }
        assertEquals(1003, map.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, map.get("key" + i));
        }
    }

    @Test
    @DisplayName("put: коллизии — разные ключи с одинаковым hashCode")
    void putCollisions() {
        class BadKey {
            final int id;

            BadKey(int id) {
                this.id = id;
            }

            @Override
            public int hashCode() {
                return 1;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof BadKey && ((BadKey) o).id == id;
            }
        }

        CustomHashMap<BadKey, String> m = new CustomHashMap<>();
        List<BadKey> keys = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            BadKey k = new BadKey(i);
            keys.add(k);
            m.put(k, "v" + i);
        }
        assertEquals(50, m.size());
        for (int i = 0; i < 50; i++) {
            assertEquals("v" + i, m.get(keys.get(i)));
        }

        for (int i = 0; i < 50; i += 2) {
            assertEquals("v" + i, m.remove(keys.get(i)));
        }
        assertEquals(25, m.size());
        for (int i = 0; i < 50; i++) {
            if (i % 2 == 0) assertNull(m.get(keys.get(i)));
            else assertEquals("v" + i, m.get(keys.get(i)));
        }
    }


    @Test
    void remove() {
        assertNull(map.remove("missing"));

        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        assertEquals(Integer.valueOf(1), map.remove("a"));
        assertFalse(map.containsKey("a"));
        assertEquals(2, map.size());


        map.put("n", null);
        assertNull(map.remove("n"));
        assertFalse(map.containsKey("n"));


        map.put(null, 7);
        assertEquals(Integer.valueOf(7), map.remove(null));
        assertFalse(map.containsKey(null));

        assertNull(map.remove("a"));
    }

    @Test
    @DisplayName("remove: удаление из середины цепочки (коллизии)")
    void removeFromCollisionChain() {
        class BadKey {
            final int id;

            BadKey(int id) {
                this.id = id;
            }

            @Override
            public int hashCode() {
                return 42;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof BadKey && ((BadKey) o).id == id;
            }
        }

        CustomHashMap<BadKey, Integer> m = new CustomHashMap<>();
        for (int i = 0; i < 10; i++) m.put(new BadKey(i), i);
        assertEquals(Integer.valueOf(5), m.remove(new BadKey(5)));
        assertNull(m.get(new BadKey(5)));
        assertEquals(9, m.size());
        for (int i = 0; i < 10; i++) {
            if (i == 5) assertNull(m.get(new BadKey(i)));
            else assertEquals(Integer.valueOf(i), m.get(new BadKey(i)));
        }
    }


    @Test
    void putAll() {
        Map<String, Integer> src = new HashMap<>();
        src.put("a", 1);
        src.put("b", 2);
        src.put("c", null);

        map.put("existing", 100);
        map.putAll(src);

        assertEquals(4, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertTrue(map.containsKey("c"));
        assertEquals(100, map.get("existing"));

        map.putAll(Collections.emptyMap());
        assertEquals(4, map.size());

        Map<String, Integer> another = Map.of("a", 99);
        map.putAll(another);
        assertEquals(99, map.get("a"));
        assertEquals(4, map.size());
    }


    @Test
    void clear() {
        for (int i = 0; i < 100; i++) map.put("k" + i, i);
        assertEquals(100, map.size());
        assertFalse(map.isEmpty());

        map.clear();

        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("k0"));
        assertFalse(map.containsKey("k0"));


        map.put("new", 1);
        assertEquals(1, map.size());
        assertEquals(1, map.get("new"));


        map.clear();
        map.clear();
        assertTrue(map.isEmpty());
    }


    @Test
    void keySet() {
        Set<String> ks = map.keySet();
        assertTrue(ks.isEmpty());
        assertEquals(0, ks.size());

        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);


        assertEquals(3, ks.size());
        assertTrue(ks.contains("a"));
        assertFalse(ks.contains("z"));


        Set<String> seen = new HashSet<>();
        for (String k : ks) seen.add(k);
        assertEquals(Set.of("a", "b", "c"), seen);


        assertTrue(ks.remove("b"));
        assertFalse(map.containsKey("b"));
        assertEquals(2, map.size());


        assertFalse(ks.remove("zzz"));

        assertSame(ks, map.keySet());


        ks.clear();
        assertTrue(map.isEmpty());
        assertTrue(ks.isEmpty());
    }

    @Test
    void keySetRemove() {
        CustomHashMap<String, String> map = new CustomHashMap<>();
        map.put("a", null);
        assertTrue(map.keySet().remove("a"));
    }

    @Test
    void values() {
        Collection<Integer> vs = map.values();
        assertTrue(vs.isEmpty());

        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 1);

        assertEquals(3, vs.size());
        assertTrue(vs.contains(1));
        assertTrue(vs.contains(2));
        assertFalse(vs.contains(99));

        List<Integer> collected = new ArrayList<>(vs);
        Collections.sort(collected);
        assertEquals(List.of(1, 1, 2), collected);


        map.put("d", 5);
        assertTrue(vs.contains(5));
        assertEquals(4, vs.size());


        vs.clear();
        assertTrue(map.isEmpty());


        assertSame(vs, map.values());
    }


    @Test
    void entrySet() {
        Set<Map.Entry<String, Integer>> es = map.entrySet();
        assertTrue(es.isEmpty());

        map.put("a", 1);
        map.put("b", 2);

        assertEquals(2, es.size());

        assertTrue(es.contains(new AbstractMap.SimpleEntry<>("a", 1)));
        assertFalse(es.contains(new AbstractMap.SimpleEntry<>("a", 999)));
        assertFalse(es.contains(new AbstractMap.SimpleEntry<>("zzz", 1)));


        for (Map.Entry<String, Integer> e : es) {
            if (e.getKey().equals("a")) {
                e.setValue(100);
            }
        }
        assertEquals(100, map.get("a"));

        assertTrue(es.remove(new AbstractMap.SimpleEntry<>("a", 100)));
        assertFalse(map.containsKey("a"));
        assertEquals(1, map.size());

        assertFalse(es.remove(new AbstractMap.SimpleEntry<>("b", 999)));
        assertTrue(map.containsKey("b"));

        assertSame(es, map.entrySet());
    }

    @Nested
    class Iterators {

        @Test
        @DisplayName("Iterator.remove() удаляет текущий элемент")
        void iteratorRemove() {
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> e = it.next();
                if (e.getValue() == 2) {
                    it.remove();
                }
            }
            assertEquals(2, map.size());
            assertFalse(map.containsKey("b"));
            assertTrue(map.containsKey("a"));
            assertTrue(map.containsKey("c"));
        }

        @Test
        @DisplayName("Iterator.remove() без next() бросает IllegalStateException")
        void iteratorRemoveWithoutNext() {
            map.put("a", 1);
            Iterator<String> it = map.keySet().iterator();
            assertThrows(IllegalStateException.class, it::remove);
        }

        @Test
        @DisplayName("Двойной remove() бросает IllegalStateException")
        void iteratorDoubleRemove() {
            map.put("a", 1);
            Iterator<String> it = map.keySet().iterator();
            it.next();
            it.remove();
            assertThrows(IllegalStateException.class, it::remove);
        }

        @Test
        @DisplayName("Fail-fast: структурная модификация во время итерации")
        void failFastOnPut() {
            map.put("a", 1);
            map.put("b", 2);

            Iterator<String> it = map.keySet().iterator();
            it.next();
            map.put("c", 3);

            assertThrows(ConcurrentModificationException.class, it::next);
        }

        @Test
        @DisplayName("Fail-fast: remove во время итерации")
        void failFastOnRemove() {
            map.put("a", 1);
            map.put("b", 2);

            Iterator<String> it = map.keySet().iterator();
            it.next();
            map.remove("b");

            assertThrows(ConcurrentModificationException.class, it::next);
        }

        @Test
        @DisplayName("Fail-fast: clear во время итерации")
        void failFastOnClear() {
            map.put("a", 1);
            map.put("b", 2);

            Iterator<String> it = map.keySet().iterator();
            it.next();
            map.clear();

            assertThrows(ConcurrentModificationException.class, it::next);
        }

        @Test
        @DisplayName("Замена значения НЕ считается структурной модификацией")
        void replaceValueDoesNotBreakIterator() {
            map.put("a", 1);
            map.put("b", 2);

            Iterator<String> it = map.keySet().iterator();
            it.next();
            map.put("a", 999);

            assertDoesNotThrow(it::next);
        }

        @Test
        @DisplayName("Итерация по всем трём view даёт одинаковый набор ключей")
        void allViewsIterateSame() {
            for (int i = 0; i < 100; i++) map.put("k" + i, i);

            Set<String> fromKeys = new HashSet<>();
            for (String k : map.keySet()) fromKeys.add(k);

            Set<String> fromEntries = new HashSet<>();
            for (Map.Entry<String, Integer> e : map.entrySet()) fromEntries.add(e.getKey());

            assertEquals(fromKeys, fromEntries);
            assertEquals(100, fromKeys.size());
        }
    }

    @Test
    @DisplayName("Сравнение с java.util.HashMap на случайных операциях")
    void randomOperationsMatchJdkHashMap() {
        CustomHashMap<Integer, Integer> mine = new CustomHashMap<>();
        Map<Integer, Integer> jdk = new HashMap<>();

        Random rnd = new Random(42);
        for (int i = 0; i < 5_000; i++) {
            int op = rnd.nextInt(3);
            int key = rnd.nextInt(500);

            switch (op) {
                case 0 -> {
                    int val = rnd.nextInt();
                    assertEquals(jdk.put(key, val), mine.put(key, val));
                }
                case 1 -> {
                    assertEquals(jdk.remove(key), mine.remove(key));
                }
                case 2 -> {
                    assertEquals(jdk.get(key), mine.get(key));
                }
            }
            assertEquals(jdk.size(), mine.size());
            assertEquals(jdk.containsKey(key), mine.containsKey(key));
        }
        assertEquals(jdk.size(), mine.size());
        for (int k = 0; k < 500; k++) {
            assertEquals(jdk.get(k), mine.get(k));
        }
    }

    @Test
    void mapEqualsAndHashCode() {
        CustomHashMap<String, Integer> a = new CustomHashMap<>();
        CustomHashMap<String, Integer> b = new CustomHashMap<>();
        a.put("x", 1);
        a.put("y", 2);
        b.put("x", 1);
        b.put("y", 2);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());


        b.put("z", 3);
        assertNotEquals(a, b);
    }
}