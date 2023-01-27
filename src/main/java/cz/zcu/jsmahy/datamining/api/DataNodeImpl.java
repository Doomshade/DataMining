package cz.zcu.jsmahy.datamining.api;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.NonNull;

import java.util.Iterator;


@Data
class DataNodeImpl<T> implements DataNode<T> {
    private static long ID_SEQ = 0;
    private final T data;
    private final DataNode<T> parent;
    private final long id;
    private String uri;
    private final ObservableList<DataNode<T>> children = FXCollections.observableArrayList();
    private String name;

    {
        this.id = ID_SEQ++;
    }

    protected DataNodeImpl() {
        this.data = null;
        this.parent = null;
    }

    DataNodeImpl(final @NonNull T data, final DataNode<T> parent) {
        this.data = data;
        this.parent = parent;
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
        assert !(child instanceof DataNodeRoot<T>);
        this.children.add(child);
    }

    @Override
    public ObservableList<DataNode<T>> getChildren() {
        return FXCollections.unmodifiableObservableList(children);
    }

    @Override
    public DataNodeRoot<T> findRoot() {
        DataNode<T> prev = parent;
        while (prev != null && prev.getParent() != null) {
            prev = prev.getParent();
        }
        return prev instanceof DataNodeRoot<T> ? (DataNodeRoot<T>) prev : null;
    }

    @Override
    public Iterator<DataNode<T>> iterator() {
        return children.iterator();
    }

    /**
     * @return Whether this node has children (aka is a parent)
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
