package git.doomshade.datamining.util;

/**
 * A basic key-value pair class
 *
 * @author Jakub Šmrha
 * @version 1.0
 * @since 1.0
 */
public class Pair<K, V> {
    public final K key;
    public final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
