package cz.zcu.jsmahy.datamining.api;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.NonNull;

import java.util.Collection;
import java.util.Iterator;


@Data
class DataNodeImpl<T> implements DataNode<T> {
    private static long ID_SEQ = 0;
    private final T data;
    private final long id;
    private String name;

    {
        this.id = ID_SEQ++;
    }

    private final ObservableList<DataNode<T>> children = FXCollections.observableArrayList();

    protected DataNodeImpl() {
        this.data = null;
    }

    DataNodeImpl(final @NonNull T data) {
        this.data = data;
    }

    @Override
    public void addChild(@NonNull DataNode<T> child) throws IllegalArgumentException, NullPointerException {
        if (child instanceof DataNodeRootImpl) {
            throw new IllegalArgumentException("Child cannot be root.");
        }
        this.children.add(child);
    }

    @Override
    public void addChildren(@NonNull Iterable<DataNode<T>> children) {
        children.forEach(this::addChild);
    }

    @Override
    public void addChildren(@NonNull Collection<DataNode<T>> children) {
        this.children.addAll(children);
    }

    @Override
    public ObservableList<DataNode<T>> getChildren() {
        return children;
    }

    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public Iterator<DataNode<T>> iterator() {
        return children.iterator();
    }
}
