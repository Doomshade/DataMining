package cz.zcu.jsmahy.datamining.api;

import lombok.NonNull;

/**
 * A factory for {@link DataNodeImpl}s.
 *
 * @author Jakub Smrha
 * @see DataNodeImpl
 * @see DataNodeRootImpl
 * @since 1.0
 */
public interface DataNodeFactory<T> {
    /**
     * Creates a new root.
     *
     * @param rootName the root display name
     *
     * @return a data node root
     */
    DataNodeRoot<T> newRoot(final String rootName);

    /**
     * Creates a new data node with the given data.
     *
     * @param data the data
     *
     * @return a new data node
     */
    DataNode<T> newNode(@NonNull T data);
}
