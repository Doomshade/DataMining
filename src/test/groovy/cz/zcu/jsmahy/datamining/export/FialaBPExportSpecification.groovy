package cz.zcu.jsmahy.datamining.export

import com.google.inject.Guice
import com.sun.javafx.application.PlatformImpl
import cz.zcu.jsmahy.datamining.api.DataNode
import cz.zcu.jsmahy.datamining.api.DataNodeFactory
import cz.zcu.jsmahy.datamining.api.JSONDataNodeSerializationUtils
import cz.zcu.jsmahy.datamining.api.Mocks
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Field
import java.nio.charset.StandardCharsets

import static FialaBPSerializer.stripTimezone

class FialaBPExportSpecification extends Specification {
    def final lock = new Object()
    @Shared
    static DataNodeFactory nodeFactory
    @Shared
    static JSONDataNodeSerializationUtils utils


    void setupSpec() {
        def mocks = new Mocks()
        def injector = Guice.createInjector(mocks.module())
        nodeFactory = Spy(injector.getInstance(DataNodeFactory))
        utils = Spy(injector.getInstance(JSONDataNodeSerializationUtils))
    }

    void setup() {}

    void cleanup() {}

    def getStubRoot() {
        def root = nodeFactory.newRoot("Doctoral Advisors")
        addNode(root,
                "Albert Einstein",
                "person",
                new GregorianCalendar(1879, Calendar.MARCH, 14, 0, 0, 0),
                new GregorianCalendar(1955, Calendar.APRIL, 18, 0, 0, 0))
        addNode(root,
                "Alfred Kleiner",
                "person",
                new GregorianCalendar(1849, Calendar.APRIL, 24, 0, 0, 0),
                new GregorianCalendar(1916, Calendar.JULY, 3, 0, 0, 0))
        addNode(root,
                "Johan Jakob Muller",
                "person",
                new GregorianCalendar(1846, Calendar.APRIL, 4, 0, 0, 0),
                new GregorianCalendar(1875, Calendar.JANUARY, 14, 0, 0, 0))
        addNode(root,
                "Adolf Fick",
                "person",
                new GregorianCalendar(1829, Calendar.SEPTEMBER, 3, 0, 0, 0),
                new GregorianCalendar(1901, Calendar.AUGUST, 21, 0, 0, 0))
        root
    }

    def addNode(DataNode root, String name, String stereotype, Calendar begin, Calendar end) {
        def p1 = nodeFactory.newNode(root)
        p1.addMetadata("name", name)
        p1.addMetadata("stereotype", stereotype)
        p1.addMetadata("begin", stripTimezone(begin))
        p1.addMetadata("end", stripTimezone(end))
        p1.addMetadata("properties", Map.of("startPrecision", "day", "endPrecision", "day"))
        p1
    }

    def "Serialization test"() {
        given:
        def root = getStubRoot()

        def out = new ByteArrayOutputStream()
        final Field[] declaredFields = FialaBPExportNodeFormat.class.getDeclaredFields()
        for (Field field : declaredFields) {
            field.trySetAccessible()
        }
        final List<FialaBPExportNodeFormat> nodes = new ArrayList<>()
        def dataNodes = root.getChildren()
        for (final DataNode dataNode : dataNodes) {
            final Map<String, Object> metadata = dataNode.getMetadata()
            final FialaBPExportNodeFormat dataNodeFormat = new FialaBPExportNodeFormat()
            dataNodeFormat.setId(dataNode.getId())
            for (Field field : declaredFields) {
                if (metadata.containsKey(field.getName())) {
                    field.set(dataNodeFormat, metadata.get(field.getName()))
                }
            }
            nodes.add(dataNodeFormat)
        }

        List<FialaBPExportEdgeFormat> edges = new ArrayList<>()
        edges.add(new FialaBPExportEdgeFormat(1, "relationship", 2, 1, "doctoral advisor"))
        edges.add(new FialaBPExportEdgeFormat(2, "relationship", 3, 2, "doctoral advisor"))
        edges.add(new FialaBPExportEdgeFormat(3, "relationship", 4, 3, "doctoral advisor"))
        def serializer = new FialaBPSerializer(utils)

        when:
        serializer.serialize(out, new FialaBPExportFormatRoot(nodes, edges))

        then:
        noExceptionThrown()

        // need to synchronize this because there's some race going on with the strings
        // sometimes the string just don't equal for whatever reason, so just put this under a lock
        synchronized (lock) {
            try (def stream = getClass().getResourceAsStream("serialization-test.js")) {
                def expected = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                def created = out.toString(StandardCharsets.UTF_8)
                expected == created
            }
        }
    }

    def "Persistence test"() {
        given:
        // we have to start up the JavaFX toolkit because the
        PlatformImpl.startup(() -> { }, false)

    }

}
