package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;

/**
 * This node represents a single node in the tree with a link to next nodes.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public interface DataNode<T> extends Iterable<DataNode<T>> {

    /**
     * NOTE: the children are UNMODIFIABLE
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
     * @return The parent of this data node or {@code null} if this node is root. If this returns {@code null} it <b>should</b> be guaranteed this is a {@link DataNodeRoot}.
     */
    DataNode<T> getParent();

    DataNodeRoot<T> findRoot();
}
