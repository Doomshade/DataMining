package cz.zcu.jsmahy.datamining.api

class StubSparqlEndpointTask<T, R> extends DefaultSparqlEndpointTask<T, R> {
    StubSparqlEndpointTask(final ApplicationConfiguration<T, R> config, final DataNodeFactory nodeFactory, final String query, final DataNodeRoot dataNodeRoot) {
        super(config, nodeFactory, query, dataNodeRoot)
    }

    @Override
    R call() throws Exception {
        throw new UnsupportedOperationException("Stub!")
    }
}
