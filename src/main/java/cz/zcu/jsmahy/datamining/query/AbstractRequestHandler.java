package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;


public abstract class AbstractRequestHandler<T, R> extends Service<R> implements RequestHandler<T, R> {
    private String query;
    private TreeItem<DataNode<T>> treeRoot;

    @Override
    public final Service<R> query(final String query, final TreeItem<DataNode<T>> treeRoot) throws InvalidQueryException {
        this.query = query;
        this.treeRoot = treeRoot;
        return this;
    }

    @Override
    protected Task<R> createTask() {
        return new Task<>() {
            @Override
            protected R call() {
                return internalQuery(query, treeRoot);
            }
        };
    }

    /**
     * The internal query request called in the {@link Task} that's created by this {@link Service}.
     *
     * @param query
     * @param treeRoot
     *
     * @return Anything that the subclasses of this want to return.
     *
     * @throws InvalidQueryException A convenience exception if the request has invalid parameters.
     * @see Service#createTask()
     */
    protected abstract R internalQuery(final String query, final TreeItem<DataNode<T>> treeRoot) throws InvalidQueryException;
}
