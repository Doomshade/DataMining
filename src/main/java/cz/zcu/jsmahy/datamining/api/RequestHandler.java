package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;

/**
 * <p>The implementations are then responsible for building the whole tree of descendents. It should start off by asking for the initial query, for example "Albert Einstein", and then the path it
 * should follow, for example "doctoral advisor". Afterwards restrictions are added to narrow the search and ambiguous input.</p>
 * <p>The implementation </p>
 *
 * @param <R> The return type of {@link Service}'s {@link Service#getValue()} in {@link #createBackgroundService(String, DataNodeRoot)}. In other words, the return type of the request.
 * @param <T> The input type for the {@link DataNode}s. In other words, the input type of the request.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface RequestHandler<T, R> {

    /**
     * Queries a SPARQL endpoint based on the handler implementation.
     *
     * @param query        the query
     * @param dataNodeRoot the tree root
     *
     * @return a service that runs on the background to find the ontology
     *
     * @throws InvalidQueryException if the request is invalid
     */
    Service<R> createBackgroundService(final String query, final DataNodeRoot<T> dataNodeRoot) throws InvalidQueryException;

    /**
     * Attempts to continue the search if the monitor is in wait queue. This is a helper method to notify the monitor.
     */
    void unlockDialogPane();
}
