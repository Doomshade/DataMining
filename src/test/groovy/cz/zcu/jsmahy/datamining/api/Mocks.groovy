package cz.zcu.jsmahy.datamining.api

import com.google.inject.Module
import spock.mock.DetachedMockFactory

class Mocks {
    private final mockFactory = new DetachedMockFactory()

    Module module() {
        def config = mockFactory.Mock(ApplicationConfiguration.class)
        def taskProvider = mockFactory.Mock(SparqlEndpointTaskProvider.class)
        module(config, taskProvider)
    }

    Module module(ApplicationConfiguration<?, ?> config, SparqlEndpointTaskProvider<?, ?, ? extends ApplicationConfiguration<?, ?>> taskProvider) {
        new DataMiningModule() {
            @Override
            protected void configure() {
                super.configure()
                bind(ApplicationConfiguration.class).toInstance(config)
                bind(SparqlEndpointTaskProvider.class).toInstance(taskProvider)
            }
        }
    }
}
