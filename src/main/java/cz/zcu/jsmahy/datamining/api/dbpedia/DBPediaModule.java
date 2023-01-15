package cz.zcu.jsmahy.datamining.api.dbpedia;

import cz.zcu.jsmahy.datamining.api.AmbiguousInputResolver;
import cz.zcu.jsmahy.datamining.api.DataMiningModule;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.UserAssistedAmbiguousInputResolver;
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
        bind(RequestHandler.class).to(DBPediaRequestHandler.class);
        bind(AmbiguousInputResolver.class).to(UserAssistedAmbiguousInputResolver.class);
    }
}
