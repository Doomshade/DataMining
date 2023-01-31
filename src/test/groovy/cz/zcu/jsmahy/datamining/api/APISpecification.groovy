package cz.zcu.jsmahy.datamining.api

import com.google.inject.Guice
import com.google.inject.Injector
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.BiConsumer

import static cz.zcu.jsmahy.datamining.api.ApplicationConfiguration.*

class APISpecification extends Specification {
    @Shared
    static DataNodeFactory<?> nodeFactory
    @Shared
    static Injector injector
    @Shared
    private DataNodeRoot<?> root
    @Shared
    private ApplicationConfiguration<?, ?> config
    @Shared
    private DefaultSparqlEndpointTask<?, ?> defaultTask
    @Shared
    private SparqlEndpointAgent<?, ?> endpointAgent

    void setupSpec() {
        def config = Mock(ApplicationConfiguration)
        config.get(BASE_URL) >> "https://baseurltest.com/"
        config.getList(IGNORE_PATH_PREDICATES) >> new ArrayList()
        config.getList(VALID_DATE_FORMATS) >> new ArrayList<>()
        def mocks = new Mocks()
        def taskProvider = Mock(SparqlEndpointTaskProvider.class)
        def task = Mock(SparqlEndpointTask)
        task.call() >> {
            Thread.sleep(30_000)
            null
        }
        taskProvider.createTask(_, _, _, _) >> task

        injector = Guice.createInjector(mocks.module(config, taskProvider))
        nodeFactory = Spy(injector.getInstance(DataNodeFactory))
    }

    void setup() {
        this.root = nodeFactory.newRoot("Root")
        this.config = injector.getInstance(ApplicationConfiguration)
        this.defaultTask = new DefaultSparqlEndpointTask(config, nodeFactory, "queryTest", root)
        this.endpointAgent = injector.getInstance(SparqlEndpointAgent.class)
    }

    def "Should throw NPE when passing null reference when trying to create a new data node"() {
        when:
        nodeFactory.newNode(null, this.root)

        then:
        thrown(NullPointerException)
    }


    def "Should return the passed value if the value was nonnull"() {
        when:
        def node = nodeFactory.newNode(_, this.root)

        then:
        node.getData() == _
    }

    def "Data node root should return false because we did not add a child to it"() {
        expect:
        !this.root.hasChildren()
        this.root.getChildren().isEmpty()
    }
// TODO: test throws UnsupportedOperationException
//
//    def "Data node should find root"() {
//        when:
//        def result = node.findRoot()
//
//        then:
//        result.id == root.id
//
//        where:
//        node << [nodeFactory.newNode("A", root),
//                 nodeFactory.newNode("A", nodeFactory.newNode("B", root)),
//                 nodeFactory.newNode("A", nodeFactory.newNode("B", nodeFactory.newNode("C", root)))]
//    }

    def "Data node root should have no parent"() {
        expect:
        this.root.getParent() == null
    }

    def "Should automatically set name if the node is an RDFNode"() {
        given:
        def rdfNode = Mock(RDFNode)
        rdfNode.isLiteral() >> true
        rdfNode.isURIResource() >> false

        def literalMock = Mock(Literal)
        literalMock.toString() >> "Test RDFNode name.@en"
        rdfNode.asLiteral() >> literalMock

        def result

        when:
        result = nodeFactory.newNode(rdfNode, this.root)

        then:
        result.name == "Test RDFNode name."
    }

    def "Should throw NPE when passing null parameters to SparqlEndpointTask ctor"() {
        when:
        new DefaultSparqlEndpointTask(appConfig, dataNodeFactory, query, dataNodeRoot)
        then:
        thrown(NullPointerException)

        where:
        appConfig | dataNodeFactory | query   | dataNodeRoot
        null      | nodeFactory     | "Query" | this.root
        config    | null            | "Query" | this.root
        config    | nodeFactory     | null    | this.root
        config    | nodeFactory     | "Query" | null
    }

    def "Should return correct query given any URL"() {
        when:
        def task = new DefaultSparqlEndpointTask(config, nodeFactory, query, root)

        then:
        task.query == "https://baseurltest.com/queryTest"

        where:
        query << ["https://baseurltest.com/queryTest", "queryTest"]
    }

    def "Should throw UOE when using call method"() {
        when:
        defaultTask.call()

        then:
        thrown(UnsupportedOperationException)
    }

    def "Should return all valid date types if the type in the collection is \"any\""() {
        given:
        config.getList(VALID_DATE_FORMATS).add("any")

        when:
        // create a new task because we are testing the constructor
        def task = new DefaultSparqlEndpointTask(config, nodeFactory, "queryTest", root)

        then:
        task.validDateFormats.containsAll(CollectionConstants.getAllValidDateFormats())
    }

    def "Should automatically set name if the node is a Resource"() {
        given:
        def rdfNode = Mock(RDFNode)
        rdfNode.isLiteral() >> false
        rdfNode.isURIResource() >> true

        def resourceMock = Mock(Resource)
        resourceMock.getURI() >> "Test URI."
        rdfNode.asResource() >> resourceMock

        def result

        when:
        result = nodeFactory.newNode(rdfNode, this.root)
        println result.getName()

        then:
        result.name == "Test URI."
    }

    def "Should add children to the data node using Iterable"() {
        given:
        def nodes = new ArrayList<DataNode<?>>()

        when:
        nodes.add(nodeFactory.newNode("A", this.root))
        nodes.add(nodeFactory.newNode("B", this.root))
        nodes.add(nodeFactory.newNode("C", this.root))

        then:
        this.root.getChildren().size() == nodes.size()
    }

    def "Should add children to the data node using Collection"() {
        given:
        def nodes = new ArrayList<DataNode<?>>()

        when:
        nodes.add(nodeFactory.newNode("A", this.root))
        nodes.add(nodeFactory.newNode("B", this.root))
        nodes.add(nodeFactory.newNode("C", this.root))

        then:
        this.root.getChildren().size() == nodes.size()
    }

    def "Should return a valid iterator"() {
        when:
        nodeFactory.newNode(_, this.root)

        then:
        this.root.iterator().hasNext()
    }

    def "Should iterate through children of root with correct order"() {
        given: "Root with children where some children may have their own children to demonstrate the iteration order."
        BiConsumer<DataNode<?>, Integer> consumer = Mock()

        def firstNode = nodeFactory.newNode("Test 1", this.root)

        def secondNode = nodeFactory.newNode("Test 2", this.root)

        def firstChildNode = nodeFactory.newNode("Test 21", secondNode)
        def secondChildNode = nodeFactory.newNode("Test 22", secondNode)

        def thirdNode = nodeFactory.newNode("Test 3", this.root)
        def thirdChildNode = nodeFactory.newNode("Test 31", thirdNode)
        def fourthChildNode = nodeFactory.newNode("Test 32", thirdNode)

        def fourthNode = nodeFactory.newNode("Test 4", this.root)

        when: "We iterate over the children with an empty consumer"
        this.root.iterate(consumer)

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

    // TODO: rename
    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null"() {
        when:
        endpointAgent.createBackgroundService(query, treeItem)

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null        | _ as DataNodeRoot<?>
        _ as String | null
    }

    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null (2)"() {
        when:
        endpointAgent.createBackgroundService(query, treeItem).start()

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null        | _ as DataNodeRoot<?>
        _ as String | null
    }


    def "Should throw IAE if query is empty"() {
        when:
        endpointAgent.createBackgroundService("", _ as DataNodeRoot<?>)
        then:
        thrown(IllegalArgumentException)
    }
}
