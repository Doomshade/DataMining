package cz.zcu.jsmahy.datamining.api;

import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Getter;

import static java.util.Objects.requireNonNull;

public class SparqlEndpointAgent<T, R> {
    @Getter
    private final SparqlEndpointTaskProvider<T, R, ApplicationConfiguration<T, R>> sparqlEndpointTaskProvider;
    @Getter
    private final ApplicationConfiguration<T, R> config;
    @Getter
    private final DataNodeFactory<T> dataNodeFactory;

    /**
     * Reason for this parameter not having a generic parameter: {@link DataMiningModule}
     */
    @Inject
    @SuppressWarnings("unchecked, rawtypes")
    public SparqlEndpointAgent(final ApplicationConfiguration config, final DataNodeFactory dataNodeFactory, final SparqlEndpointTaskProvider sparqlEndpointTaskProvider) {
        this.config = requireNonNull(config);
        this.dataNodeFactory = requireNonNull(dataNodeFactory);
        this.sparqlEndpointTaskProvider = requireNonNull(sparqlEndpointTaskProvider);
    }

    public Service<R> createBackgroundService(final String query, final DataNodeRoot<T> dataNodeRoot) {
        return new Service<>() {
            @Override
            protected Task<R> createTask() {
                return sparqlEndpointTaskProvider.createTask(config, dataNodeFactory, query, dataNodeRoot);
            }
        };
    }

    //
//    public final String getQuery() {
//        return query;
//    }
}
