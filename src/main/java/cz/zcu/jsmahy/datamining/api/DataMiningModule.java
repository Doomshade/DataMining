package cz.zcu.jsmahy.datamining.api;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import cz.zcu.jsmahy.datamining.config.Config;
import org.apache.jena.rdf.model.RDFNode;

import static com.google.inject.Scopes.SINGLETON;

/**
 * The base module for this project.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public abstract class DataMiningModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DataNodeFactory.class).to(DataNodeFactoryImpl.class);
        bind(Config.class).in(SINGLETON);
    }

}
