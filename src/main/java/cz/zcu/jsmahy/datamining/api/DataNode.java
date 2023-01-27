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

    String getUri();

    void setUri(String uri);

    DataNode<T> getParent();

    DataNodeRoot<T> findRoot();
}
