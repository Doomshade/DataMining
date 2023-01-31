package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;

import java.util.Date;
import java.util.Optional;

/**
 * This node represents a single node in the tree with a link to next nodes.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public interface DataNode<T> extends Iterable<DataNode<T>> {

    /**
     * NOTE: the children are <b>unmodifiable</b>
     *
     * @return The children of this node.
     */
    ObservableList<DataNode<T>> getChildren();

    /**
     * @return The data this node carries.
     */
    T getData();

    /**
     * @return The ID of this data node.
     */
    long getId();

    /**
     * @return The display name of the root. If null then there's no display name.
     */
    String getName();

    /**
     * Sets the display name of the root. If null then there's no display name.
     *
     * @param name the display name.
     */
    void setName(String name);

    /**
     * @return The URI of this data node.
     */
    String getUri();

    /**
     * @param uri The URI of this data node.
     */
    void setUri(String uri);

    /**
     * @return The start date.
     */
    Date getStartDate();

    /**
     * @param date The start date
     */
    void setStartDate(Date date);

    /**
     * @return The end date.
     */
    Date getEndDate();

    /**
     * @param date The end date
     */
    void setEndDate(Date date);

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
     * Adds an additional metadata value stored under the key
     *
     * @param key   the key
     * @param value the value
     */
    void addMetadata(String key, Object value);

    /**
     * @return The parent of this data node or {@code null} if this node is root. If this returns {@code null} it <b>should</b> be guaranteed this is a {@link DataNodeRoot}.
     */
    DataNode<T> getParent();

    /**
     * Attempts to find this node's {@link DataNodeRoot}. If this node is {@link DataNodeRoot} it returns {@link Optional#empty()}, otherwise this should not be empty.
     *
     * @return The root of this node.
     */
    Optional<DataNodeRoot<T>> findRoot();
}
