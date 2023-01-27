package cz.zcu.jsmahy.datamining.dbpedia;

import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTask;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTaskProvider;
import org.apache.jena.rdf.model.RDFNode;

public class DBPediaEndpointTaskProvider<T extends RDFNode, R extends Void> implements SparqlEndpointTaskProvider<T, R, DBPediaApplicationConfiguration<T, R>> {
    @Override
    public SparqlEndpointTask<T, R, DBPediaApplicationConfiguration<T, R>> createTask(final DBPediaApplicationConfiguration<T, R> config,
                                                                                      final DataNodeFactory<T> nodeFactory,
                                                                                      final String query,
                                                                                      final DataNodeRoot<T> dataNodeRoot) {
        return new DBPediaEndpointTask<>(config, nodeFactory, query, dataNodeRoot);
    }
}
