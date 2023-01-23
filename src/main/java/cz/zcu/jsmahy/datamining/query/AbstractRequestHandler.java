package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.DataMiningModule;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.RequestProgressListener;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

import static java.util.Objects.requireNonNull;


public abstract class AbstractRequestHandler<T, R> extends Service<R> implements RequestHandler<T, R> {
    protected final RequestProgressListener<T> progressListener;
    private String query;
    private TreeItem<DataNode<T>> treeRoot;

    /**
     * Reason for this parameter not having a generic parameter: {@link DataMiningModule}
     *
     * @param progressListener the progress listener
     */
    @SuppressWarnings("unchecked, rawtypes")
    protected AbstractRequestHandler(final RequestProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public final Service<R> query(final String query, final TreeItem<DataNode<T>> treeRoot) throws InvalidQueryException {
        requireNonNull(query);
        requireNonNull(treeRoot);
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
     * @param query    the query
     * @param treeRoot the tree root
     *
     * @return Anything that the subclasses of this want to return.
     *
     * @throws InvalidQueryException A convenience exception if the request has invalid parameters.
     * @see Service#createTask()
     */
    protected abstract R internalQuery(final String query, final TreeItem<DataNode<T>> treeRoot) throws InvalidQueryException;
}
