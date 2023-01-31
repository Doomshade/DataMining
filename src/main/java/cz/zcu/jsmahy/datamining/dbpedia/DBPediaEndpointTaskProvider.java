package cz.zcu.jsmahy.datamining.dbpedia;

import cz.zcu.jsmahy.datamining.api.*;
import org.apache.jena.rdf.model.RDFNode;

public class DBPediaEndpointTaskProvider<T extends RDFNode, R extends Void> implements SparqlEndpointTaskProvider<T, R> {
    @Override
    public SparqlEndpointTask<T, R> createTask(final ApplicationConfiguration<T, R> config,
                                               final DataNodeFactory<T> nodeFactory,
                                               final String query,
                                               final DataNodeRoot<T> dataNodeRoot) {
        return new DBPediaEndpointTask<>(config, nodeFactory, query, dataNodeRoot);
    }
}
