package cz.zcu.jsmahy.datamining.api;

public interface SparqlEndpointTaskProvider<T, R> {
    SparqlEndpointTask<T, R> createTask(ApplicationConfiguration<T, R> config, DataNodeFactory<T> nodeFactory, String query, DataNodeRoot<T> dataNodeRoot);
}
