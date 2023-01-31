package cz.zcu.jsmahy.datamining.api;

public interface SparqlEndpointTaskProvider<T, R> {
    SparqlEndpointTask<T, R> createTask(ApplicationConfiguration<T, R> config, DataNodeFactory nodeFactory, String query, DataNodeRoot dataNodeRoot);
}
