package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * This node represents a single node in the tree with a link to next nodes.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public interface DataNode extends Iterable<DataNode> {

    /**
     * NOTE: the children are <b>unmodifiable</b>
     *
     * @return The children of this node.
     */
    ObservableList<DataNode> getChildren();

    /**
     * @return The ID of this data node.
     */
    long getId();

    /**
     * @param key the key
     * @param <V> the value type
     *
     * @return the value stored under the key
     *
     * @throws ClassCastException if the value type is incorrect
     */
    <V> Optional<V> getMetadataValue(String key) throws ClassCastException;

    /**
     * @param key the key
     * @param <V> the value type
     *
     * @return the value stored under the key
     *
     * @throws NoSuchElementException if no such key is mapped to a variable
     * @throws ClassCastException     if the value type is incorrect
     */
    <V> V getMetadataValueUnsafe(String key) throws NoSuchElementException, ClassCastException;

    /**
     * @param key          the key
     * @param defaultValue the default value if no such key is mapped to a variable
     * @param <V>          the value type
     *
     * @return the value stored under the key
     *
     * @throws ClassCastException if the value type is incorrect
     */

    <V> V getMetadataValue(String key, V defaultValue) throws ClassCastException;

    /**
     * Adds an additional metadata value stored under the key
     *
     * @param key   the key
     * @param value the value
     */
    void addMetadata(String key, Object value);

    /**
     * Adds additional metadata values
     *
     * @param metadata the metadata values
     */
    void addMetadata(Map<String, Object> metadata);

    /**
     * @return The parent of this data node or {@code null} if this node is root. If this returns {@code null} it <b>should</b> be guaranteed this is a {@link DataNodeRoot}.
     */
    DataNode getParent();

    /**
     * Attempts to find this node's {@link DataNodeRoot}. If this node is {@link DataNodeRoot} it returns {@link Optional#empty()}, otherwise this should not be empty.
     *
     * @return The root of this node.
     */
    Optional<DataNodeRoot> findRoot();
}
