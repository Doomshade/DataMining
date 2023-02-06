package cz.zcu.jsmahy.datamining.api;

import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.Objects.requireNonNull;

/**
 * TODO: javadoc
 *
 * @param <R>
 */
public class SparqlEndpointAgent<R> {
    private static final Logger LOGGER = LogManager.getLogger(SparqlEndpointAgent.class);
    @Getter
    private final SparqlEndpointTaskProvider<R> sparqlEndpointTaskProvider;

    /**
     * Reason for this parameter not having a generic parameter: {@link DataMiningModule}
     */
    @Inject
    @SuppressWarnings("unchecked, rawtypes")
    public SparqlEndpointAgent(final SparqlEndpointTaskProvider sparqlEndpointTaskProvider) {
        this.sparqlEndpointTaskProvider = requireNonNull(sparqlEndpointTaskProvider);
    }


    public Service<R> createBackgroundService(final String query, final DataNode dataNodeRoot) throws NullPointerException, IllegalArgumentException {
        requireNonNull(query);
        requireNonNull(dataNodeRoot);
        if (query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be blank.");
        }
        LOGGER.debug("Creating a background service for {} with root {}", query, dataNodeRoot);
        return new Service<>() {
            @Override
            protected Task<R> createTask() {
                return sparqlEndpointTaskProvider.newTask(query, dataNodeRoot);
            }
        };
    }

    //
//    public final String getQuery() {
//        return query;
//    }
}
