package cz.zcu.jsmahy.datamining.api;

public interface SparqlEndpointTaskProvider<T, R, CFG extends ApplicationConfiguration<T, R>> {
    SparqlEndpointTask<T, R, CFG> createTask(CFG config, DataNodeFactory<T> nodeFactory, String query, DataNodeRoot<T> dataNodeRoot);
}
