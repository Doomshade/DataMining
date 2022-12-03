package cz.zcu.jsmahy.datamining.api;

/**
 * A factory for {@link DataNode}s.
 *
 * @author Jakub Smrha
 * @see DataNode
 * @see DataNodeRoot
 * @since 1.0
 */
public interface DataNodeFactory {
    /**
     * Creates a new root.
     *
     * @param <T> the data type
     *
     * @return a data node root
     */
    <T> DataNodeRoot<T> newRoot();

    /**
     * Creates a new data node with the given data.
     *
     * @param data the data
     * @param <T>  the data type
     *
     * @return a new data node
     */
    <T> DataNode<T> newNode(T data);
}