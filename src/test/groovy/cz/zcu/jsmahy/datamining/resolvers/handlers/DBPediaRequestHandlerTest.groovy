package cz.zcu.jsmahy.datamining.resolvers.handlers

import com.google.inject.Guice
import com.google.inject.Injector
import cz.zcu.jsmahy.datamining.api.DataNodeFactory
import cz.zcu.jsmahy.datamining.api.DataNodeRoot
import cz.zcu.jsmahy.datamining.api.Mocks
import cz.zcu.jsmahy.datamining.api.SparqlEndpointAgent
import org.apache.jena.rdf.model.RDFNode
import spock.lang.Shared
import spock.lang.Specification

class DBPediaRequestHandlerTest extends Specification {
    @Shared
    static Injector injector
    @Shared
    private SparqlEndpointAgent<RDFNode, Void> endpointAgent

    @Shared
    static DataNodeFactory<RDFNode> nodeFactory

    DataNodeRoot<RDFNode> dataNodeRoot

    void setupSpec() {
        def mocks = new Mocks()
        injector = Guice.createInjector(mocks.module())
        nodeFactory = injector.getInstance(DataNodeFactory)
    }

    void setup() {
        endpointAgent = injector.getInstance(SparqlEndpointAgent.class)
        dataNodeRoot = nodeFactory.newRoot("Root")
    }

    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null"() {
        when:
        endpointAgent.createBackgroundService(query, dataNodeRoot as DataNodeRoot<RDFNode>).start()

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null        | _ as DataNodeRoot<RDFNode>
        _ as String | null
    }
}
