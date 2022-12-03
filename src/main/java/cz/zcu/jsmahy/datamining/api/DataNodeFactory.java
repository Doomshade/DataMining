package cz.zcu.jsmahy.datamining.api;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since
 */
public interface DataNodeFactory {
    <T> DataNodeRoot<T> newRoot(T data);

    <T> DataNode<T> newNode(T data);
}
