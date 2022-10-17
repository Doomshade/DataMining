package cz.zcu.jsmahy.datamining.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A basic key-value pair class
 *
 * @author Jakub Šmrha
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class Pair<K, V> {
	private final K key;
	private final V value;
}
