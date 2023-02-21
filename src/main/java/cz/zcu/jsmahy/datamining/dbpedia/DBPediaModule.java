package cz.zcu.jsmahy.datamining.dbpedia;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import cz.zcu.jsmahy.datamining.api.DataMiningModule;
import cz.zcu.jsmahy.datamining.api.ResponseResolver;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTaskProvider;
import cz.zcu.jsmahy.datamining.resolvers.MultipleItemChoiceResolver;
import cz.zcu.jsmahy.datamining.resolvers.OntologyPathPredicateResolver;
import cz.zcu.jsmahy.datamining.resolvers.StartAndEndDateResolver;

import static com.google.inject.Scopes.SINGLETON;

/**
 * <p>Module for <a href="https://www.dbpedia.org/">DBPedia</a> SPARQL queries</p>
 *
 * @author Jakub Šmrha
 * @see DataMiningModule
 * @since 1.0
 */
public class DBPediaModule extends AbstractModule {
    protected void configure() {
        // the main request handler with its progress listener
        bind(SparqlEndpointTaskProvider.class).to(DBPediaEndpointTaskProvider.class)
                                              .in(SINGLETON);
        // ambiguous input resolvers
        bind(ResponseResolver.class).annotatedWith(Names.named("userAssisted"))
                                    .to(MultipleItemChoiceResolver.class)
                                    .in(SINGLETON);
        bind(ResponseResolver.class).annotatedWith(Names.named("ontologyPathPredicate"))
                                    .to(OntologyPathPredicateResolver.class)
                                    .in(SINGLETON);
        bind(ResponseResolver.class).annotatedWith(Names.named("date"))
                                    .to(StartAndEndDateResolver.class)
                                    .in(SINGLETON);
    }
}
