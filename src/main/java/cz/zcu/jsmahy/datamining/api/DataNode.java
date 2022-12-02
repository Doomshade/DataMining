package cz.zcu.jsmahy.datamining.api;

/**
 * This node represents a single node in the tree with a link to next nodes.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public abstract class DataNode<T> {
    public abstract T data();

    public abstract void addChild(DataNode<T> child);

    protected abstract DataNodeList<T> next();

    protected abstract boolean hasNext();
}
