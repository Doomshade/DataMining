package cz.zcu.jsmahy.datamining.request.handlers

import cz.zcu.jsmahy.datamining.api.AmbiguousInputResolver
import cz.zcu.jsmahy.datamining.api.DataNodeFactory
import cz.zcu.jsmahy.datamining.api.DataNodeRoot
import cz.zcu.jsmahy.datamining.api.RequestProgressListener
import cz.zcu.jsmahy.datamining.config.DataMiningConfiguration
import org.apache.jena.rdf.model.RDFNode
import spock.lang.Shared
import spock.lang.Specification

class DBPediaRequestHandlerTest extends Specification {
    @Shared
    private DBPediaRequestHandler<RDFNode, Void> requestHandler

    void setup() {
        requestHandler = new DBPediaRequestHandler<>(Mock(RequestProgressListener.class) as RequestProgressListener,
                Mock(DataNodeFactory.class) as DataNodeFactory,
                Mock(AmbiguousInputResolver.class),
                Mock(AmbiguousInputResolver.class),
                Mock(DataMiningConfiguration.class))
    }

    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null"() {
        when:
        requestHandler.createBackgroundService(query, treeItem as DataNodeRoot<RDFNode>).start()

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null | _ as DataNodeRoot<RDFNode>
        _ as String | null
    }

    def "Should throw ISE if we're already requesting"() {
        when:
        requestHandler.createBackgroundService("Albert_Einstein", _ as DataNodeRoot<RDFNode>).start()
        requestHandler.createBackgroundService("Albert_Einstein", _ as DataNodeRoot<RDFNode>).start()

        then:
        thrown(IllegalStateException)
    }
}
