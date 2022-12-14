package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;

import java.util.LinkedList;
import java.util.List;

/**
 * Convenience linked list implementation for data nodes.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class DataNodeList<T> extends ModifiableObservableListBase<DataNode<T>> implements ObservableList<DataNode<T>> {
    private final List<DataNode<T>> delegate = new LinkedList<>();

    @Override
    public DataNode<T> get(final int index) {
        return delegate.get(index);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    protected void doAdd(final int index, final DataNode<T> element) {
        delegate.add(index, element);
    }

    @Override
    protected DataNode<T> doSet(final int index, final DataNode<T> element) {
        return delegate.set(index, element);
    }

    @Override
    protected DataNode<T> doRemove(final int index) {
        return delegate.remove(index);
    }
}
