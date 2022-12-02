package cz.zcu.jsmahy.datamining.guice;

import com.google.inject.AbstractModule;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.handlers.DBPediaRequestHandler;

/**
 * <p>Module for <a href="https://www.dbpedia.org/">DBPedia</a> SPARQL queries</p>
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class DBPediaModule extends AbstractModule {
    protected void configure() {
        bind(RequestHandler.class)
                .to(DBPediaRequestHandler.class);
    }

}
