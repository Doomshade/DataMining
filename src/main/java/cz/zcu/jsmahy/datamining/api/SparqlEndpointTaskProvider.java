package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Service;

/**
 * TODO: javadoc
 *
 * @param <R>
 */
public interface SparqlEndpointTaskProvider<R> {
    /**
     * @param query
     * @param dataNodeRoot
     *
     * @return
     */
    SparqlEndpointTask<R> newTask(String query, DataNode dataNodeRoot);

    Service<R> createBackgroundService(String query, DataNode dataNodeRoot);
}
