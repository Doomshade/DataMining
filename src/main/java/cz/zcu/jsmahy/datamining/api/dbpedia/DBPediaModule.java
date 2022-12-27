package cz.zcu.jsmahy.datamining.api.dbpedia;

import cz.zcu.jsmahy.datamining.api.DataMiningModule;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.handlers.DBPediaRequestHandler;
import org.apache.jena.rdf.model.RDFNode;

/**
 * <p>Module for <a href="https://www.dbpedia.org/">DBPedia</a> SPARQL queries</p>
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class DBPediaModule extends DataMiningModule {
    protected void configure() {
        super.configure();
        bind(RequestHandler.class)
                .to(DBPediaRequestHandler.class);
    }

}
