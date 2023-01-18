package cz.zcu.jsmahy.datamining.query.handlers

import cz.zcu.jsmahy.datamining.api.DataNode
import javafx.scene.control.TreeItem
import org.apache.jena.rdf.model.RDFNode
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DBPediaRequestHandlerTest extends Specification {
    @Shared
    private DBPediaRequestHandler<RDFNode, Void> requestHandler

    void setup() {
        requestHandler = new DBPediaRequestHandler<>()
    }
//
//    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null"() {
//        when:
//        requestHandler.query(query, treeItem).start()
//
//        then:
//        thrown(NullPointerException)
//
//        where:
//        query       | treeItem
//        null        | _ as TreeItem<RDFNode>
//        _ as String | null
//    }
//
//    def "Should throw ISE if we're already requesting"() {
//        when:
//        requestHandler.query("Albert_Einstein", new TreeItem<DataNode<RDFNode>>()).start()
//        requestHandler.query("Albert_Einstein", new TreeItem<DataNode<RDFNode>>()).start()
//
//        then:
//        thrown(IllegalStateException)
//    }
//
//    def "Should return something"() {
//
//        when:
//        def res = requestHandler.internalQuery("Albert_Einstein", new TreeItem<DataNode<RDFNode>>())
//
//        then:
//        res == null
//    }
}
