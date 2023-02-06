package cz.zcu.jsmahy.datamining.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import cz.zcu.jsmahy.datamining.api.ArbitraryDataHolder;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;
import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_RELATIONSHIPS;
import static cz.zcu.jsmahy.datamining.export.FialaBPMetadataKeys.*;

public class FialaBPExport {
    private static final Logger LOGGER = LogManager.getLogger(FialaBPExport.class);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class DataNodeExportNodeFormat {
        private long id = -1;
        private String stereotype = null;
        private String name = null;
        private String description = null;
        private Calendar begin = null;
        private Calendar end = null;
        private Map<String, String> properties = null;
        private List<SubItem> subItems = null;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Data
        public static class SubItem {
            private long id = -1;
            private String name = null;
            private String type = null;
            private Date begin = null;
            private Date end = null;
            private String css = null;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataNodeExportEdgeFormat {
        private long id = -1;
        private String stereotype = null;
        private long from = -1;
        private long to = -1;
        private String name = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @RequiredArgsConstructor
    public static class DataNodeExportFormatRoot {
        private final List<DataNodeExportNodeFormat> nodes;
        private final List<DataNodeExportEdgeFormat> edges;
    }

    public static class FialaBPSerializer extends DataNodeSerializer<Void> {

        private int processedNodes = 0;

        public FialaBPSerializer(final OutputStream out, final DataNode root) {
            super(out, root);
        }

        private static void stripTimezone(final Calendar calendar, final DataNodeExportNodeFormat node) {
            final TimeZone tz = calendar.getTimeZone();
            // add the offset value of the timezone
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            calendar.setTimeZone(TimeZone.getTimeZone(TimeZone.getAvailableIDs(0)[0]));

        }

        public void serialize(DataNodeExportFormatRoot root) throws IOException {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
            mapper.setDateFormat(new StdDateFormat());
            mapper.writeValue(out, root);
        }

        private void processNode(final long max) {
            this.processedNodes++;
            updateProgress(this.processedNodes, max);
        }

        @Override
        protected Void call() throws Exception {
            this.processedNodes = 0;
            final List<DataNode> dataNodes = root.getChildren();
            final List<DataNodeExportNodeFormat> nodes = getNodes(dataNodes);
            final List<DataNodeExportEdgeFormat> edges = getEdges(dataNodes);
            final boolean allProcessed = this.processedNodes == dataNodes.size() * 2L;
            assert allProcessed;
            final DataNodeExportFormatRoot root = new DataNodeExportFormatRoot(nodes, edges);
            serialize(root);
            return null;
        }

        private List<DataNodeExportEdgeFormat> getEdges(final List<DataNode> dataNodes) {
            final List<DataNodeExportEdgeFormat> edges = new ArrayList<>();
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
                    edges.add(new DataNodeExportEdgeFormat(id++, stereotype, from, to, name));
                    processNode(dataNodes.size() * 2L);
                }
            }
            return edges;
        }

        private List<DataNodeExportNodeFormat> getNodes(final List<DataNode> dataNodes) throws IllegalAccessException {
            final Field[] declaredFields = DataNodeExportNodeFormat.class.getDeclaredFields();
            for (Field field : declaredFields) {
                field.trySetAccessible();
            }
            final List<DataNodeExportNodeFormat> nodes = new ArrayList<>();
            for (final DataNode dataNode : dataNodes) {
                final Map<String, Object> metadata = dataNode.getMetadata();
                final DataNodeExportNodeFormat dataNodeFormat = new DataNodeExportNodeFormat();
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
            for (final DataNodeExportNodeFormat node : nodes) {
                final Map<String, String> props = node.getProperties();
                final Calendar calendar = node.begin;
                stripTimezone(calendar, node);
            }
            return nodes;
        }
    }
}
