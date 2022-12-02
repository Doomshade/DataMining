package cz.zcu.jsmahy.datamining.api;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
class DataNodeImpl<T> extends DataNode<T> {
    private final T data;
    private final DataNodeList<T> children = new DataNodeList<>();

    DataNodeImpl(T data) {
        this.data = data;
    }

    @Override
    public T data() {
        return data;
    }

    @Override
    public void addChild(final DataNode<T> child) {
        if (child instanceof DataNodeRoot) {
            throw new IllegalArgumentException("Child cannot be root!");
        }
        children.add(child);
    }

    @Override
    protected boolean hasNext() {
        return !children.isEmpty();
    }

    @Override
    protected DataNodeList<T> next() {
        return children;
    }
}
