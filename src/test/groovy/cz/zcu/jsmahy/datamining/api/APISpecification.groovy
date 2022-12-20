package cz.zcu.jsmahy.datamining.api

import com.google.inject.Guice
import com.google.inject.Injector
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.BiConsumer

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
        root = nodeFactory.newRoot(null)
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

    def "Should throw IAE when adding root to the children of any node type"() {
        when: "Root is added to a data node"
        node.addChild(root)

        then: "Throw an IAE because that's not allowed"
        thrown(IllegalArgumentException)

        where: "Node is any node type, aka root or a regular node"
        node << [nodeFactory.newRoot(null), nodeFactory.newNode(_)]
    }

    def "Should iterate through children of root with correct order"() {
        given: "Root with children where some children may have their own children to demonstrate the iteration order."
        BiConsumer<DataNode<?>, Integer> consumer = Mock()

        def firstNode = nodeFactory.newNode("Test 1")
        root.addChild(firstNode)

        def secondNode = nodeFactory.newNode("Test 2")

        def firstChildNode = nodeFactory.newNode("Test 21")
        def secondChildNode = nodeFactory.newNode("Test 22")
        secondNode.addChild(firstChildNode)
        secondNode.addChild(secondChildNode)
        root.addChild(secondNode)

        def thirdNode = nodeFactory.newNode("Test 3")
        def thirdChildNode = nodeFactory.newNode("Test 31")
        def fourthChildNode = nodeFactory.newNode("Test 32")
        thirdNode.addChild(thirdChildNode)
        thirdNode.addChild(fourthChildNode)
        root.addChild(thirdNode)

        def fourthNode = nodeFactory.newNode("Test 4")
        root.addChild(fourthNode)

        when: "We iterate over the children with an empty consumer"
        root.iterate(consumer)

        then: "By definition the order of iterated children should be: Test 1, Test 2, Test 21, Test 22, Test 3, Test 31, Test 32, Test 4."
        1 * consumer.accept(firstNode, 0)
        then:
        1 * consumer.accept(secondNode, 0)
        then:
        1 * consumer.accept(firstChildNode, 1)
        then:
        1 * consumer.accept(secondChildNode, 1)
        then:
        1 * consumer.accept(thirdNode, 0)
        then:
        1 * consumer.accept(thirdChildNode, 1)
        then:
        1 * consumer.accept(fourthChildNode, 1)
        then:
        1 * consumer.accept(fourthNode, 0)

    }

}
