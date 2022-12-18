package cz.zcu.jsmahy.datamining.api;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Iterator;

/**
 * This node represents a single node in the tree with a link to next nodes.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DataNode<T> implements Iterable<DataNode<T>> {
    @NonNull
    private final T data;
    private final ObservableList<DataNode<T>> children = FXCollections.observableArrayList();

    protected DataNode() {
        this.data = null;
    }

    /**
     * Adds a child to this node.
     *
     * @param child the child to add to this node
     *
     * @throws IllegalArgumentException if the child is an instance of {@link DataNodeRoot}
     * @throws NullPointerException     if the child is {@code null}
     */
    public void addChild(@NonNull DataNode<T> child) throws IllegalArgumentException, NullPointerException {
        if (child instanceof DataNodeRoot) {
            throw new IllegalArgumentException("Child cannot be root.");
        }
        this.children.add(child);
    }

    public void addChildren(@NonNull Iterable<DataNode<T>> children) {
        children.forEach(this::addChild);
    }

    public void addChildren(@NonNull Collection<DataNode<T>> children) {
        this.children.addAll(children);
    }

    /**
     * @return The children of this node.
     */
    public ObservableList<DataNode<T>> getChildren() {
        return FXCollections.unmodifiableObservableList(children);
    }

    /**
     * @return Whether this node has children (aka is a parent)
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public Iterator<DataNode<T>> iterator() {
        return children.iterator();
    }
}
