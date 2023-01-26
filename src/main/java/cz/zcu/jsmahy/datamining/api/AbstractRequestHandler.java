package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.config.DataMiningConfiguration;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import static java.util.Objects.requireNonNull;


public abstract class AbstractRequestHandler<T, R> extends Service<R> implements RequestHandler<T, R> {
    protected final RequestProgressListener<T> progressListener;
    protected final DataNodeFactory<T> nodeFactory;
    protected final AmbiguousInputResolver<T, R, ?> ambiguousInputResolver;
    protected final AmbiguousInputResolver<T, R, ?> ontologyPathPredicateInputResolver;
    protected final AmbiguousInputResolver<T, R, ?> dateInputResolver;
    protected final DataMiningConfiguration configuration;
    protected String query;
    protected DataNodeRoot<T> dataNodeRoot;

    /**
     * Reason for this parameter not having a generic parameter: {@link DataMiningModule}
     *
     * @param progressListener the progress listener
     */
    @SuppressWarnings("unchecked, rawtypes")
    protected AbstractRequestHandler(final RequestProgressListener progressListener,
                                     final DataNodeFactory nodeFactory,
                                     final AmbiguousInputResolver ambiguousInputResolver,
                                     final AmbiguousInputResolver ontologyPathPredicateInputResolver,
                                     final AmbiguousInputResolver dateInputResolver,
                                     final DataMiningConfiguration configuration) {
        this.progressListener = progressListener;
        this.nodeFactory = nodeFactory;
        this.ambiguousInputResolver = ambiguousInputResolver;
        this.ontologyPathPredicateInputResolver = ontologyPathPredicateInputResolver;
        this.dateInputResolver = dateInputResolver;
        this.configuration = configuration;
    }

    public synchronized void unlockDialogPane() {
        notify();
    }

    @Override
    public final Service<R> createBackgroundService(final String query, final DataNodeRoot<T> dataNodeRoot) throws InvalidQueryException {
        this.query = requireNonNull(query);
        this.dataNodeRoot = requireNonNull(dataNodeRoot);
        return this;
    }

    @Override
    protected Task<R> createTask() {
        return new Task<>() {
            @Override
            protected R call() {
                return internalQuery();
            }
        };
    }

    /**
     * The internal query request called in the {@link Task} that's created by this {@link Service}.
     *
     * @return Anything that the subclasses of this want to return.
     *
     * @throws InvalidQueryException A convenience exception if the request has invalid parameters.
     * @see Service#createTask()
     */
    protected abstract R internalQuery() throws InvalidQueryException;
}
