package cz.zcu.jsmahy.datamining.api

import com.google.inject.Guice
import com.google.inject.Injector
import spock.lang.Shared
import spock.lang.Specification

class APISpecification extends Specification {
    @Shared
    static Injector injector
    @Shared
    static DataNodeFactory<?> nodeFactory

    private DataNodeRoot<?> root;

    void setupSpec() {
        injector = Guice.createInjector(new DataMiningModule() {})
        nodeFactory = injector.getInstance(DataNodeFactory)
    }

    void setup() {
        root = nodeFactory.newRoot()
    }

    def "Should throw NPE when passing null reference when trying to create a new data node"() {
        when:
        nodeFactory.newNode(null)

        then:
        thrown(NullPointerException)
    }


    def "Should return the passed value if the value was nonnull"() {
        when:
        def node = nodeFactory.newNode(_)

        then:
        node.getData() == _
    }

    def "Data node root should return false because we did not add a child to it"() {
        expect:
        !root.hasChildren()
        root.getChildren().isEmpty()
    }

    def "Data node root should return true because we added a child"() {
        when:
        root.addChild(nodeFactory.newNode(_))

        then:
        root.hasChildren()
        !root.getChildren().isEmpty()
    }

    def "Should throw NPE when passing in null child to a data node (root)"() {
        when:
        root.addChild(null)

        then:
        thrown(NullPointerException)
    }

    def "Should throw NPE when passing in null iterable"() {
        when:
        root.addChildren((Iterable<DataNodeImpl>) null)

        then:
        thrown(NullPointerException)
    }

    def "Should throw NPE when passing in null collection"() {
        when:
        root.addChildren((Collection<DataNodeImpl>) null)

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

}
