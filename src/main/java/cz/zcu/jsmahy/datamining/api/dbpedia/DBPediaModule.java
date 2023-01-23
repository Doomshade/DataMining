package cz.zcu.jsmahy.datamining.api.dbpedia;

import cz.zcu.jsmahy.datamining.api.DataMiningModule;
import cz.zcu.jsmahy.datamining.api.RequestProgressListener;
import cz.zcu.jsmahy.datamining.app.controller.MainController;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.handlers.DBPediaRequestHandler;
import cz.zcu.jsmahy.datamining.query.handlers.OntologyPathPredicateInputResolver;
import lombok.SneakyThrows;

import static com.google.inject.Scopes.SINGLETON;

/**
 * <p>Module for <a href="https://www.dbpedia.org/">DBPedia</a> SPARQL queries</p>
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class DBPediaModule extends DataMiningModule {
    @SneakyThrows
    protected void configure() {
        super.configure();
        bind(RequestProgressListener.class).to(MainController.class);
        bind(RequestHandler.class).to(DBPediaRequestHandler.class);
        bind(OntologyPathPredicateInputResolver.class).in(SINGLETON);
    }
}
