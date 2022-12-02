package cz.zcu.jsmahy.datamining.api;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class DataNodeFactory {

    public <T> DataNodeRoot<T> newRoot(final T data) {
        return new DataNodeRootImpl<>(data);
    }

    public <T> DataNode<T> newNode(final T data) {
        return new DataNodeImpl<>(data);
    }
}
