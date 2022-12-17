package cz.zcu.jsmahy.datamining.api.dbpedia

import com.google.inject.Guice
import cz.zcu.jsmahy.datamining.api.DataNode
import cz.zcu.jsmahy.datamining.api.DataNodeFactory
import cz.zcu.jsmahy.datamining.query.RequestHandler
import cz.zcu.jsmahy.datamining.query.SparqlRequest
import spock.lang.Specification

class APISpecification extends Specification {
    def "Data node value should not be null"() {
        given:
        def injector = Guice.createInjector(new DBPediaModule())
        def nodeFactory = injector.getInstance(DataNodeFactory.class)
        when:
        nodeFactory.newNode(null)

        then:
        thrown(NullPointerException)
    }

    def "Data Node Root should not be able to be added as a children"() {
        given:
        def injector = Guice.createInjector(new DBPediaModule())
        def nodeFactory = injector.getInstance(DataNodeFactory.class)
        def root = nodeFactory.newRoot()
        def node = nodeFactory.newNode("")

        when:
        node.addChild(root)

        then:
        thrown(IllegalArgumentException)
    }

    def "should demonstrate given-when-then"() {
        given:
        def dataNode = new DataNode("")

        when:
        String data = dataNode.data

        then:
        data == ""
    }

    def "Query should be nonnull"() {
        given:
        def injector = Guice.createInjector(new DBPediaModule());
        def dbPediaRequestHandler = injector.getInstance(RequestHandler.class);

        when:
        dbPediaRequestHandler.query(null);

        then:
        thrown(NullPointerException)
    }

    def "SPARQL Parameters should be nonnull"() {
        when:
        new SparqlRequest("", "", "", null, null)

        then:
        thrown(NullPointerException)
    }
}
