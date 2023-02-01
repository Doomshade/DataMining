package cz.zcu.jsmahy.datamining.dbpedia;

import cz.zcu.jsmahy.datamining.api.ApplicationConfiguration;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTask;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTaskProvider;

public class DBPediaEndpointTaskProvider<R> implements SparqlEndpointTaskProvider<R> {
    @Override
    public SparqlEndpointTask<R> createTask(final ApplicationConfiguration<R> config,
                                            final String query,
                                            final DataNodeRoot dataNodeRoot) {
        return new DBPediaEndpointTask<>(config, query, dataNodeRoot);
    }
}
