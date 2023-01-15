package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
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
     * @param query
     * @param treeRoot
     *
     * @return the ontology
     *
     * @throws InvalidQueryException if the request is invalid
     */
    Service<R> query(final String query, final TreeItem<DataNode<T>> treeRoot) throws InvalidQueryException;
}
