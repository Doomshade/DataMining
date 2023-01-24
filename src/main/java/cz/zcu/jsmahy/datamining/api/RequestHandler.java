package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import cz.zcu.jsmahy.datamining.query.SparqlRequest;
import javafx.concurrent.Service;
import javafx.scene.control.TreeItem;

/**
 * A handler for query requests
 *
 * @param <R> The return type of {@link Service}'s {@link Service#getValue()} in {@link #query(String, TreeItem)}. In other words, the return type of the request.
 * @param <T> The input type for the {@link SparqlRequest}. In other words, the input type of the request.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface RequestHandler<T, R> {

    /**
     * Queries a SPARQL endpoint based on the handler implementation.
     *
     * @param query    the query
     * @param treeRoot the tree root
     *
     * @return a service that runs on the background to find the ontology
     *
     * @throws InvalidQueryException if the request is invalid
     */
    Service<R> query(final String query, final TreeItem<DataNode<T>> treeRoot) throws InvalidQueryException;

    /**
     * Attempts to continue the search if the monitor is in wait queue. This is a helper method to notify the monitor.
     */
    void unlockDialogPane();
}
