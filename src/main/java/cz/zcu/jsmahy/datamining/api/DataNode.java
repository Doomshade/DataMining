package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;
import lombok.NonNull;

import java.util.Collection;

/**
 * This node represents a single node in the tree with a link to next nodes.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public interface DataNode<T> extends Iterable<DataNode<T>> {
    /**
     * Adds a child to this node.
     *
     * @param child the child to add to this node
     *
     * @throws IllegalArgumentException if the child is an instance of {@link DataNodeRoot}
     * @throws NullPointerException     if the child is {@code null}
     */
    void addChild(@NonNull DataNode<T> child) throws IllegalArgumentException, NullPointerException;

    /**
     * Adds children to this node.
     *
     * @param children the children to add
     *
     * @throws IllegalArgumentException if the child is an instance of {@link DataNodeRoot}
     * @throws NullPointerException     if the child is {@code null}
     */
    void addChildren(@NonNull Iterable<DataNode<T>> children) throws IllegalArgumentException, NullPointerException;

    /**
     * Adds children to this node.
     *
     * @param children the children to add
     *
     * @throws IllegalArgumentException if the child is an instance of {@link DataNodeRoot}
     * @throws NullPointerException     if the child is {@code null}
     */
    void addChildren(@NonNull Collection<DataNode<T>> children) throws IllegalArgumentException, NullPointerException;

    /**
     * NOTE: the children are MUTABLE
     *
     * @return The children of this node.
     */
    ObservableList<DataNode<T>> getChildren();

    /**
     * @return Whether this node has children (aka is a parent)
     */
    boolean hasChildren();

    /**
     * @return The data this node carries.
     */
    T getData();

    /**
     * @return The ID of this data node.
     */
    long getId();
}
