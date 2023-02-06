package cz.zcu.jsmahy.datamining.export

import com.google.inject.Guice
import cz.zcu.jsmahy.datamining.api.DataNode
import cz.zcu.jsmahy.datamining.api.DataNodeFactory
import cz.zcu.jsmahy.datamining.api.Mocks
import spock.lang.Specification

import java.lang.reflect.Field

class FialaBPExportSpecification extends Specification {
    static DataNodeFactory nodeFactory

    void setupSpec() {
        def mocks = new Mocks()
        def injector = Guice.createInjector(mocks.module())
        nodeFactory = Spy(injector.getInstance(DataNodeFactory))
    }

    void setup() {}

    void cleanup() {}

    def addNode(DataNode root, String name, String stereotype, Calendar begin, Calendar end) {
        def p1 = nodeFactory.newNode(root)
        p1.addMetadata("name", name)
        p1.addMetadata("stereotype", stereotype)
        p1.addMetadata("begin", begin)
        p1.addMetadata("end", end)
        p1.addMetadata("properties", Map.of("startPrecision", "day", "endPrecision", "day"))
        p1
    }

    def "Test"() {
        given:
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

        def out = System.out
        final Field[] declaredFields = FialaBPExport.DataNodeExportNodeFormat.class.getDeclaredFields()
        for (Field field : declaredFields) {
            field.trySetAccessible()
        }
        final List<FialaBPExport.DataNodeExportNodeFormat> nodes = new ArrayList<>()
        def dataNodes = root.getChildren()
        for (final DataNode dataNode : dataNodes) {
            final Map<String, Object> metadata = dataNode.getMetadata()
            final FialaBPExport.DataNodeExportNodeFormat dataNodeFormat = new FialaBPExport.DataNodeExportNodeFormat()
            dataNodeFormat.setId(dataNode.getId())
            for (Field field : declaredFields) {
                if (metadata.containsKey(field.getName())) {
                    field.set(dataNodeFormat, metadata.get(field.getName()))
                }
            }
            nodes.add(dataNodeFormat)
        }

        List<FialaBPExport.DataNodeExportEdgeFormat> edges = new ArrayList<>()
        edges.add(new FialaBPExport.DataNodeExportEdgeFormat(1, "relationship", 2, 1, "doctoral advisor"))
        edges.add(new FialaBPExport.DataNodeExportEdgeFormat(2, "relationship", 3, 2, "doctoral advisor"))
        edges.add(new FialaBPExport.DataNodeExportEdgeFormat(3, "relationship", 4, 3, "doctoral advisor"))
        def serializer = new FialaBPExport.FialaBPSerializer(out, root)

        when:
        serializer.serialize(new FialaBPExport.DataNodeExportFormatRoot(nodes, edges))

        then:
        noExceptionThrown()
    }

}
