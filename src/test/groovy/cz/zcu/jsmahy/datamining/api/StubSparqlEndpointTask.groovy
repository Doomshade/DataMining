package cz.zcu.jsmahy.datamining.api

class StubSparqlEndpointTask<T, R> extends DefaultSparqlEndpointTask<R> {
    StubSparqlEndpointTask(final ApplicationConfiguration<R> config, final DataNodeFactory nodeFactory, final String query, final DataNode dataNodeRoot) {
        super(config, query, dataNodeRoot)
    }

    @Override
    R call() throws Exception {
        throw new UnsupportedOperationException("Stub!")
    }
}
