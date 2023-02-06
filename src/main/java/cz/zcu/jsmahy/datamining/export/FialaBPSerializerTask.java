package cz.zcu.jsmahy.datamining.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import cz.zcu.jsmahy.datamining.api.ArbitraryDataHolder;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeSerializerTask;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;
import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_RELATIONSHIPS;
import static cz.zcu.jsmahy.datamining.export.FialaBPMetadataKeys.*;

class FialaBPSerializerTask extends DataNodeSerializerTask {
    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    static {
        JSON_OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JSON_OBJECT_MAPPER.enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
        JSON_OBJECT_MAPPER.setDateFormat(new StdDateFormat());
//        JSON_OBJECT_MAPPER.enable(SerializationFeature.CLOSE_CLOSEABLE);
    }

    private int processedNodes = 0;

    public FialaBPSerializerTask(final OutputStream out, final DataNode root) {
        super(out, root);
    }

    private static void stripTimezone(final Calendar calendar, final FialaBPExportNodeFormat node) {
        final TimeZone tz = calendar.getTimeZone();
        // add the offset value of the timezone
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        calendar.setTimeZone(TimeZone.getTimeZone(TimeZone.getAvailableIDs(0)[0]));
        // TODO: implement
    }

    // method for testing purposes
    void serialize(FialaBPExportFormatRoot root) throws IOException {
        JSON_OBJECT_MAPPER.writeValue(out, root);
        out.close();
    }

    private void processNode(final long max) {
        this.processedNodes++;
        updateProgress(this.processedNodes, max);
    }

    @Override
    protected Void call() throws Exception {
        this.processedNodes = 0;
        final List<DataNode> dataNodes = root.getChildren();
        final List<FialaBPExportNodeFormat> nodes = getNodes(dataNodes);
        final List<FialaBPExportEdgeFormat> edges = getEdges(dataNodes);
        final boolean allProcessed = this.processedNodes == dataNodes.size() * 2L;
        assert allProcessed;
        final FialaBPExportFormatRoot root = new FialaBPExportFormatRoot(nodes, edges);
        serialize(root);
        return null;
    }

    private List<FialaBPExportEdgeFormat> getEdges(final List<DataNode> dataNodes) {
        final List<FialaBPExportEdgeFormat> edges = new ArrayList<>();
        long id = 1;
        for (DataNode dataNode : dataNodes) {
            final Optional<List<ArbitraryDataHolder>> opt = dataNode.getValue(METADATA_KEY_RELATIONSHIPS);
            if (opt.isEmpty()) {
                processNode(dataNodes.size() * 2L);
                continue;
            }
            for (final ArbitraryDataHolder relationship : opt.get()) {
                final String stereotype = relationship.getValue(METADATA_KEY_STEREOTYPE, "");
                final Long from = relationship.getValue(METADATA_KEY_FROM, -1L);
                final Long to = relationship.getValue(METADATA_KEY_TO, -1L);
                final String name = relationship.getValue(METADATA_KEY_NAME, "");
                edges.add(new FialaBPExportEdgeFormat(id++, stereotype, from, to, name));
                processNode(dataNodes.size() * 2L);
            }
        }
        return edges;
    }

    private List<FialaBPExportNodeFormat> getNodes(final List<DataNode> dataNodes) throws IllegalAccessException {
        final Field[] declaredFields = FialaBPExportNodeFormat.class.getDeclaredFields();
        for (Field field : declaredFields) {
            field.trySetAccessible();
        }
        final List<FialaBPExportNodeFormat> nodes = new ArrayList<>();
        for (final DataNode dataNode : dataNodes) {
            final Map<String, Object> metadata = dataNode.getMetadata();
            final FialaBPExportNodeFormat dataNodeFormat = new FialaBPExportNodeFormat();
            dataNodeFormat.setId(dataNode.getId());
            for (Field field : declaredFields) {
                if (metadata.containsKey(field.getName())) {

                    final Object value = metadata.get(field.getName());
                    field.set(dataNodeFormat, value);
                }
            }
            nodes.add(dataNodeFormat);
            processNode(dataNodes.size() * 2L);
        }

        // we need to remove the timezone from the value and keep the date before setting it
        // if the date precision is "day"
        // this is due to Jackson removing the timezone and adding/subtracting the amount of hours,
        // for example, if the time was 00:00 and the timezone was +1hr or more, then the date gets rolled back to the previous one with 23:00 on the clock
        // this is very likely unwanted because for birthdays we usually know the date, but not the exact hour, and so we default the time of birth to 00:00
        // in the timezone of the state the person was born in
        // to solve this we basically add the time zone value and remove the timezone. the date is then recalculated internally in Calendar
        for (final FialaBPExportNodeFormat node : nodes) {
            final Map<String, String> props = node.getProperties();
            final Calendar calendar = node.getBegin();
            stripTimezone(calendar, node);
        }
        return nodes;
    }
}
