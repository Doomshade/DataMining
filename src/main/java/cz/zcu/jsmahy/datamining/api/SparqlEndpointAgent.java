package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;

public interface SparqlEndpointAgent<T, R> {
    Service<R> createBackgroundService(String query, DataNodeRoot<T> dataNodeRoot) throws InvalidQueryException;
}
