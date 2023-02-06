package cz.zcu.jsmahy.datamining.dbpedia;

import com.google.inject.AbstractModule;
import cz.zcu.jsmahy.datamining.api.DataMiningModule;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTaskProvider;

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
    }
}
