package cz.zcu.jsmahy.datamining.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeSerializer;
import cz.zcu.jsmahy.datamining.api.Relationship;
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
import static cz.zcu.jsmahy.datamining.export.FialaBPMetadataKeys.METADATA_KEY_STEREOTYPE;

public class FialaBPExport {
    private static final Logger LOGGER = LogManager.getLogger(FialaBPExport.class);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class DataNodeExportNodeFormat {
        private long id = -1;
        private String stereotype = "";
        private String name = "";
        private String description = "";
        private Date begin = new Date();
        private Date end = new Date();
        private Map<String, String> properties = new HashMap<>();
        private List<SubItem> subItems = new ArrayList<>();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Data
        public static class SubItem {
            private long id = -1;
            private String name = "";
            private String type = "";
            private Date begin = new Date();
            private Date end = new Date();
            private String css = "";
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataNodeExportEdgeFormat {
        private long id = -1;
        private String stereotype = "";
        private long from = -1;
        private long to = -1;
        private String name = "";
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @RequiredArgsConstructor
    public static class DataNodeExportFormatRoot {
        private final List<DataNodeExportNodeFormat> nodes;
        private final List<DataNodeExportEdgeFormat> edges;
    }

    public static class FialaBPSerializer extends DataNodeSerializer<Void> {

        public FialaBPSerializer(final OutputStream out, final DataNode root) {
            super(out, root);
        }

        public void serialize(DataNodeExportFormatRoot root) throws IOException {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
            mapper.writeValue(out, root);
        }

        private int processedNodes = 0;

        private void processNode(final long max) {
            processedNodes++;
            updateProgress(processedNodes, max);
        }

        @Override
        protected Void call() throws Exception {
            processedNodes = 0;
            final List<DataNode> dataNodes = root.getChildren();
            final List<DataNodeExportNodeFormat> nodes = getNodes(dataNodes);
            final List<DataNodeExportEdgeFormat> edges = getEdges(dataNodes);
            final boolean allProcessed = processedNodes == dataNodes.size() * 2L;
            LOGGER.debug("Processed all: {}", allProcessed);
            assert allProcessed;
            final DataNodeExportFormatRoot root = new DataNodeExportFormatRoot(nodes, edges);
            serialize(root);
            return null;
        }

        private List<DataNodeExportEdgeFormat> getEdges(final List<DataNode> dataNodes) {
            final List<DataNodeExportEdgeFormat> edges = new ArrayList<>();
            long id = 1;
            for (DataNode dataNode : dataNodes) {
                final Optional<List<Relationship>> opt = dataNode.getMetadataValue(METADATA_KEY_RELATIONSHIPS);
                if (opt.isEmpty()) {
                    continue;
                }
                for (final Relationship relationship : opt.get()) {
                    final String stereotype = relationship.getMetadataValue(METADATA_KEY_STEREOTYPE, "");
                    final long from = relationship.getFrom();
                    final long to = relationship.getTo();
                    final String name = relationship.getMetadataValue(METADATA_KEY_NAME, "");
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
                        field.set(dataNodeFormat, metadata.get(field.getName()));
                    }
                }
                nodes.add(dataNodeFormat);
                processNode(dataNodes.size() * 2L);
            }
            return nodes;
        }
    }
}
