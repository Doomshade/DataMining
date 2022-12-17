package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.NonNull;


public abstract class AbstractRequestHandler<T, R> extends Service<R> implements RequestHandler<T, R> {
    private SparqlRequest<T> request;

    @Override
    public final Service<R> query(@NonNull final SparqlRequest<T> request) throws InvalidQueryException {
        this.request = request;
        return this;
    }

    @Override
    protected Task<R> createTask() {
        return new Task<>() {
            @Override
            protected R call() {
                return internalQuery(request);
            }
        };
    }

    /**
     * The internal query request called in the {@link Task} that's created by this {@link Service}.
     *
     * @param request the SPARQL request
     *
     * @return Anything that the subclasses of this want to return.
     *
     * @throws InvalidQueryException A convenience exception if the request has invalid parameters.
     * @see Service#createTask()
     */
    protected abstract R internalQuery(final SparqlRequest<T> request) throws InvalidQueryException;
}
