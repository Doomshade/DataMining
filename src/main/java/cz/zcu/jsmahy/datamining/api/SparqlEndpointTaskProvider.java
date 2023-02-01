package cz.zcu.jsmahy.datamining.api;

public interface SparqlEndpointTaskProvider<R> {
    /**
     * @param config       the application configuration
     * @param query
     * @param dataNodeRoot
     *
     * @return
     */
    SparqlEndpointTask<R> createTask(ApplicationConfiguration<R> config, String query, DataNodeRoot dataNodeRoot);
}
