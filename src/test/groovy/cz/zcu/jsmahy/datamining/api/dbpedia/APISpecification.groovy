package cz.zcu.jsmahy.datamining.api.dbpedia

import com.google.inject.Guice
import com.google.inject.Injector
import cz.zcu.jsmahy.datamining.api.DataNode
import cz.zcu.jsmahy.datamining.api.DataNodeFactory
import cz.zcu.jsmahy.datamining.api.DataNodeRoot
import cz.zcu.jsmahy.datamining.query.RequestHandler
import spock.lang.Shared
import spock.lang.Specification

class APISpecification extends Specification {
    static final Injector injector = Guice.createInjector(new DBPediaModule())
    @Shared
    static DataNodeFactory<?> nodeFactory

    DataNodeRoot<?> root;

    void setupSpec() {
        nodeFactory = injector.getInstance(DataNodeFactory)
    }

    void setup() {
        root = nodeFactory.newRoot()
    }

    def "Data node root should return false because we did not add a child to it"() {
        expect:
        !root.hasChildren()
    }

    def "Data node root should return true because we added a child"() {
        when:
        root.addChild(nodeFactory.newNode(_))

        then:
        root.hasChildren()
    }

    def "Should throw NPE when passing in null child to a data node (root)"() {
        when:
        root.addChild(null)

        then:
        thrown(NullPointerException)
    }

    def "Should throw NPE when passing in null iterable"() {
        when:
        root.addChildren((Iterable<DataNode>) null)

        then:
        thrown(NullPointerException)
    }

    def "Should throw NPE when passing in null collection"() {
        when:
        root.addChildren((Collection<DataNode>) null)

        then:
        thrown(NullPointerException)
    }

    def "Should throw IAE when adding root to the children of any node"() {
        when: "Add the root to the node"
        node.addChild(root)

        then:
        thrown(IllegalArgumentException)

        where:
        node << [nodeFactory.newRoot(), nodeFactory.newNode(_)]
    }

    def "Should throw NPE when passing null query to request handler"() {
        given:
        def requestHandler = injector.getInstance(RequestHandler.class);

        when:
        requestHandler.query(null);

        then:
        thrown(NullPointerException)
    }

    def "Should return the passed value if the value was nonnull"() {
        when:
        def node = nodeFactory.newNode(_)

        then:
        node.getData() == _
    }
}
