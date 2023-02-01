package cz.zcu.jsmahy.datamining.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeSerializer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FialaBPExport {
    private static final Logger LOGGER = LogManager.getLogger(FialaBPExport.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    private static class DataNodeExportFormat {
        private long id;
        private String stereotype;
        private String name;
        private Date begin;
        private Date end;
        private String description;
        private Map<String, String> properties;
        private List<SubItem> subItems;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Data
        private static class SubItem {
            private long id;
            private String name;
            private String type;
            private Date begin;
            private Date end;
            private String css;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @RequiredArgsConstructor
    private static class DataNodeExportFormatRoot {
        private final List<DataNodeExportFormat> nodes;
    }

    public static class FialaBPSerializer extends DataNodeSerializer<Void> {

        public FialaBPSerializer(final OutputStream out, final DataNode root) {
            super(out, root);
        }

        @Override
        protected Void call() throws Exception {
            final List<DataNode> dataNodes = root.getChildren();
            LOGGER.debug("DataNodes: {}", dataNodes);
            final Field[] declaredFields = DataNodeExportFormat.class.getDeclaredFields();
            for (Field field : declaredFields) {
                field.trySetAccessible();
            }

            final List<DataNodeExportFormat> nodes = new ArrayList<>();
            for (final DataNode dataNode : dataNodes) {
                final Map<String, Object> metadata = dataNode.getMetadata();
                final DataNodeExportFormat dataNodeFormat = new DataNodeExportFormat();
                dataNodeFormat.setId(dataNode.getId());
                for (Field field : declaredFields) {
                    if (metadata.containsKey(field.getName())) {
                        field.set(dataNodeFormat, metadata.get(field.getName()));
                    }
                }
                LOGGER.debug("Injected fields: {}", dataNodeFormat);
                nodes.add(dataNodeFormat);
            }
            final ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

            final DataNodeExportFormatRoot root = new DataNodeExportFormatRoot(nodes);
            mapper.writeValue(out, root);
            return null;
        }
    }
}
