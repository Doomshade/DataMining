package cz.zcu.jsmahy.datamining.api.dbpedia;

import com.google.inject.name.Names;
import cz.zcu.jsmahy.datamining.api.AmbiguousInputResolver;
import cz.zcu.jsmahy.datamining.api.DataMiningModule;
import cz.zcu.jsmahy.datamining.api.RequestHandler;
import cz.zcu.jsmahy.datamining.api.RequestProgressListener;
import cz.zcu.jsmahy.datamining.app.controller.MainController;
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
        bind(RequestProgressListener.class).toInstance(MainController.getInstance());
        bind(AmbiguousInputResolver.class).annotatedWith(Names.named("ontologyPathPredicate"))
                                          .to(OntologyPathPredicateInputResolver.class)
                                          .in(SINGLETON);
        bind(RequestHandler.class).to(DBPediaRequestHandler.class);
    }
}
