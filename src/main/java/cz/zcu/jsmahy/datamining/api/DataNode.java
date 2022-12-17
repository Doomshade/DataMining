package cz.zcu.jsmahy.datamining.api;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Objects;

/**
 * This node represents a single node in the tree with a link to next nodes.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DataNode<T> {
    private final T data;
    private final DataNodeList<T> children = new DataNodeList<>();

    /**
     * Adds a child to this node.
     *
     * @param child the child to add to this node
     *
     * @throws IllegalArgumentException if the child is an instance of {@link DataNodeRoot}
     * @throws NullPointerException     if the child is {@code null}
     */
    public void addChild(DataNode<T> child) throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(child);
        if (child instanceof DataNodeRoot) {
            throw new IllegalArgumentException("Child cannot be root.");
        }
        this.children.add(child);
    }

    public void addChildren(Iterable<DataNode<T>> children) {
        children.forEach(this::addChild);
    }

    public void addChildren(Collection<DataNode<T>> children) {
        this.children.addAll(children);
    }

    /**
     * @return The children of this node.
     */
    public DataNodeList<T> getChildren() {
        return children;
    }

    /**
     * @return Whether this node has children (aka is a parent)
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
