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

    Module module(ApplicationConfiguration config, SparqlEndpointTaskProvider<?> taskProvider) {
        new DataMiningModule() {
            @Override
            protected void configure() {
                super.configure()
                // override the default app config from DataMiningModule to point to our config
                // see super.configure()
                // this neat hack allows us to pass in the mock implementation as well
                // don't ask me how this works exactly because I don't know myself, but leave it as is so the test run
                bind(DefaultApplicationConfiguration.class).toInstance(config)
                bind(SparqlEndpointTaskProvider.class).toInstance(taskProvider)
            }
        }
    }
}
