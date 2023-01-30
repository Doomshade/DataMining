package cz.zcu.jsmahy.datamining.api;

import com.google.inject.Inject;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import static java.util.Objects.requireNonNull;


class SparqlEndpointAgentImpl<T, R> implements SparqlEndpointAgent<T, R> {
    private final Object lock = new Object();
    private final SparqlEndpointTaskProvider<T, R, ApplicationConfiguration<T, R>> sparqlEndpointTaskProvider;
    private final ApplicationConfiguration<T, R> config;
    private final DataNodeFactory<T> nodeFactory;
    private Service<R> service = null;

    /**
     * Reason for this parameter not having a generic parameter: {@link DataMiningModule}
     */
    @Inject
    @SuppressWarnings("unchecked, rawtypes")
    public SparqlEndpointAgentImpl(final ApplicationConfiguration config, final DataNodeFactory nodeFactory, final SparqlEndpointTaskProvider sparqlEndpointTaskProvider) {
        this.config = requireNonNull(config);
        this.nodeFactory = requireNonNull(nodeFactory);
        this.sparqlEndpointTaskProvider = requireNonNull(sparqlEndpointTaskProvider);
    }

    @Override
    public Service<R> createBackgroundService(final String query, final DataNodeRoot<T> dataNodeRoot) throws InvalidQueryException {
        synchronized (lock) {
            return service = new Service<>() {
                @Override
                protected Task<R> createTask() {
                    return sparqlEndpointTaskProvider.createTask(config, nodeFactory, query, dataNodeRoot);
                }
            };
        }
    }

    @Override
    public boolean isRequesting() {
        synchronized (lock) {
            return service != null && service.isRunning();
        }
    }


    //
//    public final String getQuery() {
//        return query;
//    }
}
