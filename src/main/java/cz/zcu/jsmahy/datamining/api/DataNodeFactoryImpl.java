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
    public DataNodeRoot<T> newRoot() {
        return new DataNodeRoot<>();
    }

    @Override
    public DataNode<T> newNode(final @NonNull T data) {
        return new DataNode<>(data);
    }
}
