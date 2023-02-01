package cz.zcu.jsmahy.datamining.api;

public interface SparqlEndpointTaskProvider<R> {
    SparqlEndpointTask<R> createTask(ApplicationConfiguration<R> config, DataNodeFactory nodeFactory, String query, DataNodeRoot dataNodeRoot);
}
