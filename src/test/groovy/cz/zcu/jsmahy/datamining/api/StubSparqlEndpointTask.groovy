package cz.zcu.jsmahy.datamining.api

class StubSparqlEndpointTask<T, R, CFG extends ApplicationConfiguration<T, R>> extends SparqlEndpointTask<T, R, CFG> {
    StubSparqlEndpointTask(final CFG config, final DataNodeFactory<T> nodeFactory, final String query, final DataNodeRoot<T> dataNodeRoot) {
        super(config, nodeFactory, query, dataNodeRoot)
    }

    @Override
    protected R call() throws Exception {
        throw new UnsupportedOperationException("Stub!")
    }
}
