package cz.zcu.jsmahy.datamining.api.dbpedia;

import cz.zcu.jsmahy.datamining.api.DataMiningModule;
import cz.zcu.jsmahy.datamining.query.AsyncRequestHandler;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.handlers.DBPediaRequestHandler;
import lombok.SneakyThrows;

/**
 * <p>Module for <a href="https://www.dbpedia.org/">DBPedia</a> SPARQL queries</p>
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class DBPediaModule extends DataMiningModule {
    @SneakyThrows
    protected void configure() {
        super.configure();
        bind(AsyncRequestHandler.class).to(DBPediaRequestHandler.class);
    }

}
