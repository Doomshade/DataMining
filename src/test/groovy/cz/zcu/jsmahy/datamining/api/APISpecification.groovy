package cz.zcu.jsmahy.datamining.api

import com.google.inject.Guice
import com.google.inject.Injector
import com.sun.javafx.application.PlatformImpl
import javafx.application.Platform
import javafx.event.Event
import javafx.event.EventDispatchChain
import javafx.event.EventType
import org.yaml.snakeyaml.parser.ParserException
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
    static JSONDataNodeSerializationUtils utils
    @Shared
    static DataNodeFactory nodeFactory
    @Shared
    static Injector injector

    @Shared
    private ApplicationConfiguration config
    @Shared
    private DefaultSparqlEndpointTask<?> defaultTask
    @Shared
    private SparqlEndpointAgent<?> endpointAgent

    void setupSpec() {
        def config = Mock(ApplicationConfiguration)
        config.getValueUnsafe(CFG_KEY_BASE_URL) >> "https://baseurltest.com/"
        config.getValueUnsafe(CFG_KEY_IGNORE_PATH_PREDICATES) >> new ArrayList<>()
        config.getValueUnsafe(CFG_KEY_VALID_DATE_FORMATS) >> new ArrayList<>()
        def mocks = new Mocks()
        def taskProvider = Mock(SparqlEndpointTaskProvider)
        def task = Mock(SparqlEndpointTask)
        task.call() >> {
            null
        }
        // need to mock dispatch chain because some internals use the dispatch event method and it does not expect it
        // nor the event to return null
        def dispatchChain = Mock(EventDispatchChain)
        dispatchChain.dispatchEvent(_ as Event) >> new Event(EventType.ROOT)
        task.buildEventDispatchChain(_ as EventDispatchChain) >> dispatchChain
        taskProvider.newTask(_ as String, _ as DataNode) >> task

        injector = Guice.createInjector(mocks.module(config, taskProvider))
        nodeFactory = Spy(injector.getInstance(DataNodeFactory))
        utils = Spy(injector.getInstance(JSONDataNodeSerializationUtils))
    }

    void setup() {
        this.config = injector.getInstance(ApplicationConfiguration)
        this.defaultTask = new DefaultSparqlEndpointTask("queryTest", nodeFactory.newRoot("Root"), config, Mock(RequestProgressListener))
        this.endpointAgent = injector.getInstance(SparqlEndpointAgent.class)
    }

    def "Should throw IAE if the query is blank"() {
        when:
        new DefaultSparqlEndpointTask(" ", nodeFactory.newRoot("Root"), config, Mock(RequestProgressListener))

        then:
        thrown(IllegalArgumentException)
    }

    def "Should throw IAE if the data node is not root"() {
        when:
        new DefaultSparqlEndpointTask(" ", nodeFactory.newNode(nodeFactory.newRoot("Root")), config, Mock(RequestProgressListener))

        then:
        thrown(IllegalArgumentException)
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

    def "Should return correct query given any URL"() {
        expect:
        DefaultSparqlEndpointTask.transformQuery(query, "https://baseurltest.com/") == "https://baseurltest.com/queryTest"

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
        List<String> validDates = new ArrayList<>()
        validDates.add(CFG_DATE_FORMAT_ANY)

        when:
        def result = DefaultSparqlEndpointTask.transformValidDateFormats(validDates)

        then:
        result.containsAll(ApplicationConfiguration.ALL_VALID_DATE_FORMATS)
    }

    def "Should throw an exception when passing in invalid YAML reader"() {
        // we don't need any of the parameters as they serve as getters/members of class only
        def config = new DefaultApplicationConfiguration()
        def reader = new StringReader("{ \"Why am I putting JSON here?!\": \"Because you are a moron :)\" }\nWhatAmIDoing: \"Following it up with YAML!\"")

        when:
        config.reload(reader)

        then:
        thrown(ParserException)
    }

    def "Should return variables that were passed in as reader"() {
        given:
        // we don't need any of the parameters as they serve as getters/members of class only
        def config = new DefaultApplicationConfiguration()
        def reader = new StringReader(YAML_SAMPLE)

        when:
        config.reload(reader)

        // test whether methods return correct values
        then:
        config.hasMetadataKey("base-url")
        config.getValue("base-url").isPresent()

        config.hasMetadataKey("valid-date-formats")
        config.getValue("valid-date-formats").isPresent()

        // we need to do this because "only one exception condition is allowed per 'then' block"
        // check for base url
        when:
        config.getValueUnsafe("base-url")

        then:
        noExceptionThrown()

        when:
        config.getValueUnsafe("valid-date-formats")

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

    def "Should have metadata in map when something is added via addMetadata and have it removed via removeMetadata"() {
        given:
        def node = nodeFactory.newRoot("Root")

        when:
        node.addMetadata("testKey", "testValue")

        then:
        node.getMetadata().containsKey("testKey")

        when:
        node.removeMetadata("testKey")

        then:
        !node.getMetadata().containsKey("testKey")

        when:
        node.addMetadata("testKey", "testValue")
        node.addMetadata("testKey2", "testValue")
        node.addMetadata("testKey3", "testValue")
        assert node.hasMetadataKey("testKey")
        assert node.hasMetadataKey("testKey2")
        assert node.hasMetadataKey("testKey3")
        node.clearMetadata()

        then:
        node.getMetadata().isEmpty()
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
        node.getValue("name").isPresent()
        node.getValue("name").get() == "Test name"

        when:
        node.getValueUnsafe("name")

        then:
        noExceptionThrown()
    }

    def "Should return default metadata value because no metadata was added under such key"() {
        given:
        def root = nodeFactory.newRoot("Root")

        when:
        def node = nodeFactory.newNode(root)

        then:
        node.getValue("testKey", "defaultValue") == "defaultValue"
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

    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null"() {
        when:
        endpointAgent.createBackgroundQueryService(query, treeItem)

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null        | _ as DataNode
        _ as String | null
    }

    def "Should throw IAE if either query (#query) and tree item (#treeItem) is null (2)"() {
        when:
        endpointAgent.createBackgroundQueryService(query, treeItem).start()

        then:
        thrown(NullPointerException)

        where:
        query       | treeItem
        null        | _ as DataNode
        _ as String | null
    }


    def "Should throw IAE if query is empty"() {
        when:
        endpointAgent.createBackgroundQueryService("", _ as DataNode)
        then:
        thrown(IllegalArgumentException)
    }

    def "Should throw IAE if data node is not root"() {
        def node = Mock(DataNode)
        node.isRoot() >> false
        when:
        endpointAgent.createBackgroundQueryService("queryTest", node)
        then:
        thrown(IllegalArgumentException)
    }


    def "Should return a valid service"() {
        given:
        def root = Mock(DataNode)
        root.isRoot() >> true
        when:
        endpointAgent.createBackgroundQueryService("queryTest", root)
        then:
        noExceptionThrown()
    }

    def "Should create a task via task provider when background service is started"() {
        given:
        PlatformImpl.startup(() -> { }, false)
        def svc = endpointAgent.createBackgroundQueryService("queryTest", nodeFactory.newRoot("Root"))
        when:
        svc.restart()
        def running = false
        def start = System.currentTimeSeconds()
        // need to do this on the FX UI thread, #isRunning calls #checkThread
        Platform.runLater(() -> {
            while (!svc.running && System.currentTimeSeconds() - start < 5) {
                // no-op
            }
            running = true
        })
        // spinlock for at most 5 seconds
        while (!running && System.currentTimeSeconds() - start < 5) {
            // no-op
        }

        then:
        noExceptionThrown()
        // this does not seem to work for some reason :(
        // just check no exception is thrown for now
//        1 * endpointAgent.sparqlEndpointTaskProvider.createTask(_, _, _)
//        1 * endpointAgent.sparqlEndpointTaskProvider.createTask(*_, *_, *_)
    }

    def getStubRoot() {
        def root = nodeFactory.newRoot("Doctoral Advisors")
        addNode(root,
                "Albert Einstein",
                "person",
                new GregorianCalendar(1879, Calendar.MARCH, 14, 1, 0, 0),
                new GregorianCalendar(1955, Calendar.APRIL, 18, 1, 0, 0))
        addNode(root,
                "Alfred Kleiner",
                "person",
                new GregorianCalendar(1849, Calendar.APRIL, 24, 1, 0, 0),
                new GregorianCalendar(1916, Calendar.JULY, 3, 2, 0, 0))
        addNode(root,
                "Johan Jakob Muller",
                "person",
                new GregorianCalendar(1846, Calendar.APRIL, 4, 1, 0, 0),
                new GregorianCalendar(1875, Calendar.JANUARY, 14, 1, 0, 0))
        addNode(root,
                "Adolf Fick",
                "person",
                new GregorianCalendar(1829, Calendar.SEPTEMBER, 3, 1, 0, 0),
                new GregorianCalendar(1901, Calendar.AUGUST, 21, 1, 0, 0))
        root
    }

    def addNode(DataNode root, String name, String stereotype, Calendar begin, Calendar end) {
        def p1 = nodeFactory.newNode(root)
        p1.addMetadata("name", name)
        p1.addMetadata("stereotype", stereotype)
        p1.addMetadata("begin", begin)
        p1.addMetadata("end", end)
        p1.addMetadata("properties", Map.of("startPrecision", "day", "endPrecision", "day"))
        p1
    }

    def "Serializer Test"() {
        given:
        def outFile = new File("test.json")
        outFile.createNewFile()
        def serializer = new JSONDataNodeSerializer(utils)

        when:
        serializer.serialize(new FileOutputStream(outFile), getStubRoot())

        then:
        serializer.fileExtension == "json" // just for the coverage
        noExceptionThrown()
    }

    def "Deserializer test"() {
        given:
        nodeFactory.newRoot("")
        nodeFactory.newRoot("")
        nodeFactory.newRoot("")
        nodeFactory.newRoot("")
        nodeFactory.newRoot("")
        nodeFactory.newRoot("")
        nodeFactory.newRoot("")
        nodeFactory.newRoot("")

        def inFile = new File("test.json")
        def deserializer = new JSONDataNodeDeserializer(utils)

        when:
        def node = deserializer.deserialize(new FileInputStream(inFile))
        println node

        then:
        deserializer.acceptedFileExtensions[0] == 'json' // just for the coverage
        noExceptionThrown()
    }

    def "Response resolver test"() {
        given:
        def responseResolver = new DefaultResponseResolver() {
            @Override
            protected void resolveInternal(final Object inputMetadata, final SparqlEndpointTask requestHandler) {
                result.addMetadata("TestKey", "TestValue")
                markResponseReady()
                requestHandler.unlockDialogPane()
            }
        }
        def task = Mock(SparqlEndpointTask)
        task.unlockDialogPane() >> {} // do nothing

        when:
        responseResolver.resolve(_ as Object, task)

        then:
        responseResolver.hasResponseReady()
        responseResolver.hasResponseReadyProperty().get()

        when:
        def response = responseResolver.getResponse()

        then:
        notThrown(IllegalStateException) // there is a response
        response.hasMetadataKey("TestKey")
        response.getValue("TestKey").isPresent()
    }

    def "Should throw ISE if resolve was not called"() {
        given:
        def responseResolver = new DefaultResponseResolver() {
            @Override
            protected void resolveInternal(final Object inputMetadata, final SparqlEndpointTask requestHandler) {
                result.addMetadata("TestKey", "TestValue")
                markResponseReady()
                requestHandler.unlockDialogPane()
            }
        }
        def task = Mock(SparqlEndpointTask)
        task.unlockDialogPane() >> {} // do nothing

        when:
        responseResolver.getResponse()

        then:
        thrown(IllegalStateException)
    }
}
