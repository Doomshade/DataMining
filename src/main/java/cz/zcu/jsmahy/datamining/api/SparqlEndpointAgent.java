package cz.zcu.jsmahy.datamining.api;

import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.Objects.requireNonNull;

/**
 * This is a helper class that provides an easy way to create a {@link Service} for the query we perform. It requires a {@link SparqlEndpointTaskProvider} that creates/provides a
 * {@link SparqlEndpointTask}s.
 *
 * @param <R> The generic type of {@link SparqlEndpointTask}
 */
public class SparqlEndpointAgent<R> {
    private static final Logger LOGGER = LogManager.getLogger(SparqlEndpointAgent.class);
    private final SparqlEndpointTaskProvider<R> sparqlEndpointTaskProvider;

    /**
     * Reason for this parameter not having a generic parameter: {@link DataMiningModule}
     *
     * @param sparqlEndpointTaskProvider the task provider that's called when creating a new {@link Service}
     *
     * @see DataMiningModule
     * @see #createBackgroundService(String, DataNode)
     */
    @Inject
    @SuppressWarnings("unchecked, rawtypes")
    public SparqlEndpointAgent(final SparqlEndpointTaskProvider sparqlEndpointTaskProvider) {
        this.sparqlEndpointTaskProvider = requireNonNull(sparqlEndpointTaskProvider);
    }

    /**
     * Creates a new background {@link Service} for the query.
     *
     * @param query        the query
     * @param dataNodeRoot the data node root to add the children to
     *
     * @return a new {@link Service} with a new {@link SparqlEndpointTask} provided by {@link SparqlEndpointTaskProvider}
     *
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if the query is blank or the data node is not a root
     */
    public Service<R> createBackgroundService(final String query, final DataNode dataNodeRoot) throws NullPointerException, IllegalArgumentException {
        requireNonNull(query);
        requireNonNull(dataNodeRoot);
        if (query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be blank.");
        }
        if (!dataNodeRoot.isRoot()) {
            throw new IllegalArgumentException("Data node must be root.");
        }
        LOGGER.debug("Creating a background service for {} with root {}", query, dataNodeRoot);
        return new Service<>() {
            @Override
            protected Task<R> createTask() {
                return sparqlEndpointTaskProvider.newTask(query, dataNodeRoot);
            }
        };
    }
}
