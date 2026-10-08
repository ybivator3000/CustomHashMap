import java.util.Collection;
import java.util.Set;

public interface CustomMap<K, V> {
    int size();
    boolean isEmpty();
    boolean containsKey();
    boolean containsValue();

    V put(K key, V value); // object key реализация в офф док
    V get(K key);
    V remove(K key);

    void clear();
    void putAll(CustomMap<K, V> map);
    Set<K> keySet();
    Collection<V> values();
    // entrySet
}
