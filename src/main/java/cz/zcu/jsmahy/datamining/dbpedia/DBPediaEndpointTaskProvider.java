package cz.zcu.jsmahy.datamining.dbpedia;

import cz.zcu.jsmahy.datamining.api.*;

public class DBPediaEndpointTaskProvider<R> implements SparqlEndpointTaskProvider<R> {
    @Override
    public SparqlEndpointTask<R> createTask(final ApplicationConfiguration<R> config,
                                            final DataNodeFactory nodeFactory,
                                            final String query,
                                            final DataNodeRoot dataNodeRoot) {
        return new DBPediaEndpointTask<>(config, nodeFactory, query, dataNodeRoot);
    }
}
