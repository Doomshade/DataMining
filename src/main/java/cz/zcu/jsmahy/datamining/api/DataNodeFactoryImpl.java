package cz.zcu.jsmahy.datamining.api;

import lombok.NonNull;

/**
 * Default implementation of {@link DataNodeFactory}.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
final class DataNodeFactoryImpl<T> implements DataNodeFactory<T> {

    @Override
    public DataNodeRootImpl<T> newRoot() {
        return new DataNodeRootImpl<>();
    }

    @Override
    public DataNode<T> newNode(final @NonNull T data) {
        return new DataNodeImpl<>(data);
    }
}
