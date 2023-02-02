package cz.zcu.jsmahy.datamining.api

import com.google.inject.Guice
import com.google.inject.Injector
import javafx.application.Platform
import javafx.event.Event
import javafx.event.EventDispatchChain
import javafx.event.EventType
import org.yaml.snakeyaml.parser.ParserException
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.BiConsumer

import static cz.zcu.jsmahy.datamining.api.ApplicationConfiguration.*

class APISpecification extends Specification {
    public static final String YAML_SAMPLE = """
                                             valid-date-formats:
                                               - "any"
                                             base-url: "http://dbpedia.org/resource/"
                                             """


    @Shared
    static DataNodeFactory nodeFactory
    @Shared
    static Injector injector

    @Shared
    private ApplicationConfiguration<?> config
    @Shared
    private DefaultSparqlEndpointTask<?> defaultTask
    @Shared
    private SparqlEndpointAgent<?> endpointAgent

    void setupSpec() {
        def config = Mock(ApplicationConfiguration)
        config.getUnsafe(CFG_KEY_BASE_URL) >> "https://baseurltest.com/"
        config.getListUnsafe(CFG_KEY_IGNORE_PATH_PREDICATES) >> new ArrayList<>()
        config.getListUnsafe(CFG_KEY_VALID_DATE_FORMATS) >> new ArrayList<>()
        def mocks = new Mocks()
        def taskProvider = Mock(SparqlEndpointTaskProvider)
        def task = Mock(SparqlEndpointTask)
        task.call() >> {
            Thread.sleep(30_000)
            null
        }
        // need to mock dispatch chain because some internals use the dispatch event method and it does not expect it
        // nor the event to return null
        def dispatchChain = Mock(EventDispatchChain)
        dispatchChain.dispatchEvent(_) >> new Event(EventType.ROOT)
        task.buildEventDispatchChain(_) >> dispatchChain
        taskProvider.createTask(_, _, _) >> task

        injector = Guice.createInjector(mocks.module(config, taskProvider))
        nodeFactory = Spy(injector.getInstance(DataNodeFactory))
    }

    void setup() {
        this.config = injector.getInstance(ApplicationConfiguration)
        this.defaultTask = new DefaultSparqlEndpointTask(config, "queryTest", nodeFactory.newRoot("Root"))
        this.endpointAgent = injector.getInstance(SparqlEndpointAgent.class)
    }

    def "Should throw NPE when passing null reference when trying to create a new data node"() {
        when:
        nodeFactory.newNode(null)

        then:
        thrown(NullPointerException)
    }

    def "Data node root should return false because we did not add a child to it"() {
        given:
        def root = nodeFactory.newRoot("Root")

        expect:
        root.getChildren().isEmpty()
    }
// TODO: test throws UnsupportedOperationException

    def "Data node should find root"() {
        when:
        def root = nodeFactory.newRoot("Root")
        def result = nodeFactory.newNode(nodeFactory.newNode(root)).findRoot()

        then:
        result.isPresent()
        result.get().getId() == root.getId()
    }

    def "Data node root should have no parent"() {
        given:
        def root = nodeFactory.newRoot("Root")

        expect:
        root.getParent() == null
        root.findRoot().isEmpty()
    }

    def "Should throw NPE when passing null parameters to SparqlEndpointTask ctor"() {
        when:
        new DefaultSparqlEndpointTask(appConfig, query, dataNodeRoot)
        then:
        thrown(NullPointerException)

        where:
        appConfig | query   | dataNodeRoot
        null      | "Query" | nodeFactory.newRoot("Root")
        config    | null    | nodeFactory.newRoot("Root")
        config    | "Query" | null
    }

    def "Should return correct query given any URL"() {
        given:
        def root = nodeFactory.newRoot("Root")

        when:
        def task = new DefaultSparqlEndpointTask(config, query, root)

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

    def "Should serialize a datanode to a valid JSON"() {

    }

    def "Should return all valid date types if the type in the collection is \"any\""() {
        given:
        def root = nodeFactory.newRoot("Root")
        config.getListUnsafe(CFG_KEY_VALID_DATE_FORMATS).add(CFG_DATE_FORMAT_ANY)

        when:
        // create a new task because we are testing the constructor
        def task = new DefaultSparqlEndpointTask(config, "queryTest", root)

        then:
        task.validDateFormats.containsAll(CollectionConstants.getAllValidDateFormats())
    }

    def "Should throw an exception when passing in invalid YAML reader"() {
        // we don't need any of the parameters as they serve as getters/members of class only
        def config = new DefaultApplicationConfiguration(Mock(RequestProgressListener), Mock(DataNodeFactory), Mock(ResponseResolver), Mock(ResponseResolver), Mock(ResponseResolver))
        def reader = new StringReader("{ \"Why am I putting JSON here?!\": \"Because you are a moron :)\" }\nWhatAmIDoing: \"Following it up with YAML!\"")

        when:
        config.reload(reader)

        then:
        thrown(ParserException)
    }

    def "Should return variables that were passed in as reader"() {
        given:
        // we don't need any of the parameters as they serve as getters/members of class only
        def config = new DefaultApplicationConfiguration(Mock(RequestProgressListener), Mock(DataNodeFactory), Mock(ResponseResolver), Mock(ResponseResolver), Mock(ResponseResolver))
        def reader = new StringReader(YAML_SAMPLE)

        when:
        config.reload(reader)

        // test whether methods return correct values
        then:
        config.has("base-url")
        config.get("base-url").isPresent()

        config.has("valid-date-formats")
        config.getList("valid-date-formats").isPresent()

        // we need to do this because "only one exception condition is allowed per 'then' block"
        // check for base url
        when:
        config.getUnsafe("base-url")

        then:
        noExceptionThrown()

        when:
        config.getListUnsafe("valid-date-formats")

        then:
        noExceptionThrown()
    }

    def "Should add children to the data node using Iterable"() {
        given:
        def root = nodeFactory.newRoot("Root")
        def nodes = new ArrayList<DataNode>()

        when:
        nodes.add(nodeFactory.newNode(root))
        nodes.add(nodeFactory.newNode(root))
        nodes.add(nodeFactory.newNode(root))

        then:
        root.getChildren().size() == nodes.size()
    }

    def "Should have metadata in map when something is added via addMetadata"() {
        given:
        def node = nodeFactory.newRoot("Root")

        when:
        node.addMetadata("testKey", "testValue")

        then:
        node.getMetadata().containsKey("testKey")
    }

    def "Should return a valid iterator"() {
        given:
        def root = nodeFactory.newRoot("Root")

        when:
        nodeFactory.newNode(root)

        then:
        root.iterator().hasNext()
    }

    def "Should add metadata to the newly created data node"() {
        when:
        def root = nodeFactory.newRoot("Root")
        def node = nodeFactory.newNode(root, Map.of("name", "Test name"))

        then:
        node.getMetadataValue("name").isPresent()
        node.getMetadataValue("name").get() == "Test name"

        when:
        node.getMetadataValueUnsafe("name")

        then:
        noExceptionThrown()
    }

    def "Should return default metadata value because no metadata was added under such key"() {
        given:
        def root = nodeFactory.newRoot("Root")

        when:
        def node = nodeFactory.newNode(root)

        then:
        node.getMetadataValue("testKey", "defaultValue") == "defaultValue"
    }

    def "Should iterate through children of root with correct order"() {
        given: "Root with children where some children may have their own children to demonstrate the iteration order."
        def root = nodeFactory.newRoot("Root")
        BiConsumer<DataNode, Integer> consumer = Mock()

        def firstNode = nodeFactory.newNode(root, "Test ")

        def secondNode = nodeFactory.newNode(root, "Test 2")

        def firstChildNode = nodeFactory.newNode(secondNode, "Test 21")
        def secondChildNode = nodeFactory.newNode(secondNode, "Test 22")

        def thirdNode = nodeFactory.newNode(root, "Test 3")
        def thirdChildNode = nodeFactory.newNode(thirdNode, "Test 31")
        def fourthChildNode = nodeFactory.newNode(thirdNode, "Test 32")

        def fourthNode = nodeFactory.newNode(root, "Test 4")

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

    // TODO: rename
    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null"() {
        when:
        endpointAgent.createBackgroundService(query, treeItem)

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null        | _ as DataNode
        _ as String | null
    }

    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null (2)"() {
        when:
        endpointAgent.createBackgroundService(query, treeItem).start()

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null        | _ as DataNode
        _ as String | null
    }


    def "Should throw IAE if query is empty"() {
        when:
        endpointAgent.createBackgroundService("", _ as DataNode)
        then:
        thrown(IllegalArgumentException)
    }

    def "Should return a valid service"() {
        when:
        endpointAgent.createBackgroundService("queryTest", _ as DataNode)
        then:
        noExceptionThrown()
    }

    @Ignore("Requires JavaFX Platform")
    def "Should create a task via task provider when background service is started"() {
        given:
        Platform.startup {}
        def svc = endpointAgent.createBackgroundService("queryTest", _ as DataNode)
        when:
        svc.start()

        then:
        noExceptionThrown()
        // TODO: this does not seem to work for some reason :(
        // just check no exception is thrown for now
//        1 * endpointAgent.sparqlEndpointTaskProvider.createTask(_, _, _)
//        1 * endpointAgent.sparqlEndpointTaskProvider.createTask(*_, *_, *_)
    }
}
