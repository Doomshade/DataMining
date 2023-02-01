package cz.zcu.jsmahy.datamining.api;

import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.Objects.requireNonNull;

public class SparqlEndpointAgent<R> {
    private static final Logger LOGGER = LogManager.getLogger(SparqlEndpointAgent.class);
    @Getter
    private final SparqlEndpointTaskProvider<R> sparqlEndpointTaskProvider;
    @Getter
    private final ApplicationConfiguration<R> config;
    @Getter
    private final DataNodeFactory dataNodeFactory;

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


    public Service<R> createBackgroundService(@NonNull final String query, @NonNull final DataNodeRoot dataNodeRoot) throws NullPointerException, IllegalArgumentException {
        if (query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be blank.");
        }
        LOGGER.debug("Creating a background service for {} with root {}", query, dataNodeRoot);
        return new Service<>() {
            @Override
            protected Task<R> createTask() {
                return sparqlEndpointTaskProvider.createTask(config, query, dataNodeRoot);
            }
        };
    }

    //
//    public final String getQuery() {
//        return query;
//    }
}
