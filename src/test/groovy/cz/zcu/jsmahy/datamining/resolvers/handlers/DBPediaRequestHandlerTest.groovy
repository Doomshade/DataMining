package cz.zcu.jsmahy.datamining.resolvers.handlers

import com.google.inject.Guice
import com.google.inject.Injector
import cz.zcu.jsmahy.datamining.api.DataMiningModule
import cz.zcu.jsmahy.datamining.api.DataNodeFactory
import cz.zcu.jsmahy.datamining.api.DataNodeRoot
import cz.zcu.jsmahy.datamining.api.SparqlEndpointAgent
import org.apache.jena.rdf.model.RDFNode
import spock.lang.Shared
import spock.lang.Specification

class DBPediaRequestHandlerTest extends Specification {
    @Shared
    static Injector injector
    @Shared
    private SparqlEndpointAgent<RDFNode, Void> requestHandler

    @Shared
    static DataNodeFactory<?> nodeFactory

    void setupSpec() {
        injector = Guice.createInjector(new DataMiningModule() {})
        nodeFactory = injector.getInstance(DataNodeFactory)
    }

    void setup() {
        requestHandler = injector.getInstance(SparqlEndpointAgent.class)
    }

    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null"() {
        when:
        requestHandler.createBackgroundService(query, dataNodeRoot).start()

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null        | _ as DataNodeRoot<RDFNode>
        _ as String | null
    }

    def "Should throw ISE if we're already requesting"() {
        when:
        requestHandler.createBackgroundService(query, dataNodeRoot).start()
        requestHandler.createBackgroundService(query, dataNodeRoot).start()

        then:
        thrown(IllegalStateException)
    }
}
