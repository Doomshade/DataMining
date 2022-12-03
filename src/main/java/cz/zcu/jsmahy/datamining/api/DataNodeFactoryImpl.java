package cz.zcu.jsmahy.datamining.api;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public final class DataNodeFactoryImpl implements DataNodeFactory {

    @Override
    public <T> DataNodeRoot<T> newRoot() {
        return new DataNodeRoot<>();
    }

    @Override
    public <T> DataNode<T> newNode(final T data) {
        return new DataNode<>(data);
    }
}
