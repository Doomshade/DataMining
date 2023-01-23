package cz.zcu.jsmahy.datamining.api;

import lombok.NonNull;

/**
 * Default implementation of {@link DataNodeFactory}.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
final class DataNodeFactoryImpl<T> implements DataNodeFactory<T> {

    @Override
    public DataNodeRoot<T> newRoot(final String rootName) {
        final DataNodeRoot<T> root = new DataNodeRootImpl<>();
        root.setName(rootName);
        return root;
    }

    @Override
    public DataNode<T> newNode(final @NonNull T data) {
        return new DataNodeImpl<>(data);
    }
}
