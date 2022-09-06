package net.n2oapp.platform.seek;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author RMakhmutov
 * @since 06.09.2022
 */
public class CollectionUtil {
    @SafeVarargs
    public static <E> List<E> listOf(E... values) {
        return Arrays.stream(values).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <E> Set<E> setOf(E... values) {
        return Arrays.stream(values).collect(Collectors.toSet());
    }

    public static <K, V> Map<K, V> mapOf(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(K key1, V value1, K key2, V value2) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }
}
