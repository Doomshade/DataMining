package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * This node represents a single node in the tree with a link to next nodes.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public interface DataNode extends Iterable<DataNode> {
    // general purpose metadata keys
    // MD prefix works as "metadata" prefix
    String MD_KEY_NAME = "name";
    String MD_KEY_URI = "uri";
    String MD_KEY_RDF_NODE = "rdfNode";


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
     * @return The parent of this data node or {@code null} if this node is root. If this returns {@code null} it <b>should</b> be guaranteed method {@link DataNode#isRoot()} returns {@code true}.
     */
    DataNode getParent();

    /**
     * Attempts to find this node's {@code root}. If this node is {@code root} it returns {@link Optional#empty()}, otherwise this should not be empty.
     *
     * @return The root of this node.
     */
    Optional<DataNode> findRoot();

    /**
     * @return {@code true} whether this node is root.
     */
    boolean isRoot();

    /**
     * <p>Iterates over the children of this root.</p>
     * <p>The first argument of the {@link BiConsumer} is the data node, the second argument is the breadth of the node in respect to the
     * parent. In essence, the breadth represents the column. For example:</p>
     * <ul>
     *     <li>(0) Karel IV</li>
     *      <ul>
     *          <li>(1) Jan Lucembursky</li>
     *          <li>(1) Ludvik Bavorsky</li>
     *      </ul>
     *      <li>(0) Jan Lucembursky</li>
     *      <li>(0) ...</li>
     * </ul>
     * <p>In this example the nodes with breadth {@code 1} are children of the parent Karel IV, whereas nodes with breadth {@code 0} are
     * children of root.
     * </p>
     * <p>The first item to consume is the root which contains a null reference to data with depth of -1.</p>
     * <p>The items to consume will be iterated in this order:</p>
     * <ul>
     *     <li>(0) Karel IV</li>
     *     <li>(1) Jan Lucembursky</li>
     *     <li>(1) Ludvik Bavorsky</li>
     *     <li>(0) Jan Lucembursky</li>
     *     <li>(0) ...</li>
     * </ul>
     * <p>Next items always have a depth of {@code >= 0}.</p>
     *
     * @param biConsumer the first parameter is the data node, the second one is the depth
     */
    void iterate(BiConsumer<DataNode, Integer> biConsumer);
}
