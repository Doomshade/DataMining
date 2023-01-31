package cz.zcu.jsmahy.datamining.resolvers.handlers

import com.google.inject.Guice
import com.google.inject.Injector
import cz.zcu.jsmahy.datamining.api.DataNodeRoot
import cz.zcu.jsmahy.datamining.api.Mocks
import cz.zcu.jsmahy.datamining.api.SparqlEndpointAgent
import spock.lang.Shared
import spock.lang.Specification

class DBPediaRequestHandlerTest extends Specification {
    @Shared
    static Injector injector
    @Shared
    private SparqlEndpointAgent<?, ?> endpointAgent

    void setupSpec() {
        def mocks = new Mocks()
        injector = Guice.createInjector(mocks.module())
    }

    void setup() {
        endpointAgent = injector.getInstance(SparqlEndpointAgent.class)
    }

    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null"() {
        when:
        endpointAgent.createBackgroundService(query, treeItem).start()

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null | _ as DataNodeRoot<?>
        _ as String | null
    }
}
