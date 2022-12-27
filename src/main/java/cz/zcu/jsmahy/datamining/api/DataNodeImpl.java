package cz.zcu.jsmahy.datamining.api;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Iterator;


@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DataNodeImpl<T> implements DataNode<T> {
    @NonNull
    private final T data;
    private final ObservableList<DataNode<T>> children = FXCollections.observableArrayList();

    protected DataNodeImpl() {
        this.data = null;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final DataNodeImpl<?> dataNode)) {
            return false;
        }

        if (!getData().equals(dataNode.getData())) {
            return false;
        }
        return getChildren().equals(dataNode.getChildren());
    }

    @Override
    public int hashCode() {
        int result = getData().hashCode();
        result = 31 * result + getChildren().hashCode();
        return result;
    }

}
