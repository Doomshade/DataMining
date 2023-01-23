package cz.zcu.jsmahy.datamining.query.handlers

import cz.zcu.jsmahy.datamining.api.AmbiguousInputResolver
import cz.zcu.jsmahy.datamining.api.DataNode
import cz.zcu.jsmahy.datamining.api.DataNodeFactory
import cz.zcu.jsmahy.datamining.api.RequestProgressListener
import javafx.scene.control.TreeItem
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
                Mock(AmbiguousInputResolver.class))
    }

    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null"() {
        when:
        requestHandler.query(query, treeItem as TreeItem<DataNode<RDFNode>>).start()

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null        | _ as TreeItem<RDFNode>
        _ as String | null
    }

    def "Should throw ISE if we're already requesting"() {
        when:
        requestHandler.query("Albert_Einstein", new TreeItem<DataNode<RDFNode>>()).start()
        requestHandler.query("Albert_Einstein", new TreeItem<DataNode<RDFNode>>()).start()

        then:
        thrown(IllegalStateException)
    }

    def "Should return something"() {

        when:
        def res = requestHandler.internalQuery("Albert_Einstein", new TreeItem<DataNode<RDFNode>>())

        then:
        res == null
    }
}
