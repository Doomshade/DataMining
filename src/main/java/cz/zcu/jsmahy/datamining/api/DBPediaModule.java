package cz.zcu.jsmahy.datamining.api;

import com.google.inject.name.Names;
import cz.zcu.jsmahy.datamining.app.controller.MainController;
import cz.zcu.jsmahy.datamining.config.DBPediaConfiguration;
import cz.zcu.jsmahy.datamining.config.DataMiningConfiguration;
import cz.zcu.jsmahy.datamining.request.handlers.DBPediaRequestHandler;
import cz.zcu.jsmahy.datamining.request.resolvers.OntologyPathPredicateInputResolver;
import cz.zcu.jsmahy.datamining.request.resolvers.StartAndEndDateInputResolver;
import cz.zcu.jsmahy.datamining.request.resolvers.UserAssistedAmbiguousInputResolver;
import lombok.SneakyThrows;

import static com.google.inject.Scopes.SINGLETON;

/**
 * <p>Module for <a href="https://www.dbpedia.org/">DBPedia</a> SPARQL queries</p>
 * TODO: move this somewhere else
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class DBPediaModule extends DataMiningModule {
    @SneakyThrows
    protected void configure() {
        super.configure();

        // ambiguous input resolvers
        bind(AmbiguousInputResolver.class).annotatedWith(Names.named("userAssisted"))
                                          .to(UserAssistedAmbiguousInputResolver.class)
                                          .in(SINGLETON);
        bind(AmbiguousInputResolver.class).annotatedWith(Names.named("ontologyPathPredicate"))
                                          .to(OntologyPathPredicateInputResolver.class)
                                          .in(SINGLETON);
        bind(AmbiguousInputResolver.class).annotatedWith(Names.named("date"))
                                          .to(StartAndEndDateInputResolver.class)
                                          .in(SINGLETON);

        // the main request handler with its progress listener
        bind(RequestHandler.class).to(DBPediaRequestHandler.class);
        bind(RequestProgressListener.class).toInstance(MainController.getInstance());

        // config
        bind(DataMiningConfiguration.class).annotatedWith(Names.named("dbpediaConfig"))
                                           .toProvider(() -> {
                                               final DataMiningConfiguration config = new DBPediaConfiguration("dbpedia-config.yml");
                                               config.reload();
                                               return config;
                                           })
                                           .in(SINGLETON);

    }
}
