package cz.zcu.jsmahy.datamining.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import cz.zcu.jsmahy.datamining.api.*;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static cz.zcu.jsmahy.datamining.Main.TOP_LEVEL_FRONTEND_DIRECTORY_NAME;
import static cz.zcu.jsmahy.datamining.api.Alerts.alertFileExists;
import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;
import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_RELATIONSHIPS;
import static cz.zcu.jsmahy.datamining.export.FialaBPMetadataKeys.*;

public class FialaBPSerializer implements DataNodeSerializer {
    private static final String DATA_JS = "fiala-bp/src/data/data.js";
    private static final String PREFIX = "define([],function(){return";
    private static final String SUFFIX = ";});\r\n";
    private static final Logger LOGGER = LogManager.getLogger(FialaBPSerializer.class);
    // this creates a copy of the object mapper
    private final ObjectMapper objectMapper;

    @Inject
    public FialaBPSerializer(JSONDataNodeSerializationUtils utils) {
        this.objectMapper = utils.getJsonObjectMapper();
    }

    static synchronized void stripTimezone(final Calendar calendar) {
        if (calendar == null) {
            return;
        }

        final TimeZone tz = calendar.getTimeZone();
        // add the offset value of the timezone and set the timezone to 0
        // the calendar class will automatically remove the offset once we set the timezone (or rather once we get the value after setting the timezone)
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void exportRoot(final DataNode dataNodeRoot,
                           @Nullable final ObservableList<Service<?>> runningServices,
                           @Nullable final ObservableList<Service<?>> finishedServices,
                           @Nullable final ObservableList<DataNode> failedNodes) throws IOException {
        // this exports to "frontend/fiala-bp/src/data.js"
        final File targetFile = new File(TOP_LEVEL_FRONTEND_DIRECTORY_NAME, DATA_JS);
        if (!targetFile.exists()) {
            if (targetFile.createNewFile()) {
                throw new IOException("Failed to create file " + targetFile);
            }
        } else {
            final AtomicReference<SerializationResponse> ref = new AtomicReference<>();
            alertFileExists(ref, targetFile.getPath());
        }
        exportRoot(targetFile, dataNodeRoot, runningServices, finishedServices, failedNodes);
    }

    @Override
    public void serialize(final OutputStream out, final DataNode root) throws IOException {
        final List<? extends DataNode> dataNodes = root.getChildren();
        final List<FialaBPExportNodeFormat> nodes = getNodes(dataNodes);
        final List<FialaBPExportEdgeFormat> edges = getEdges(dataNodes);
        final FialaBPExportFormatRoot exportRoot = new FialaBPExportFormatRoot(nodes, edges);
        serialize(out, exportRoot);
    }

    @Override
    public String getFileExtension() {
        return "js";
    }

    // method for testing purposes
    synchronized void serialize(final OutputStream out, FialaBPExportFormatRoot root) throws IOException {
        try (final PrintWriter pw = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            pw.print(PREFIX);
            objectMapper.writeValue(pw, root);
            pw.print(SUFFIX);
        }
    }

    private List<FialaBPExportEdgeFormat> getEdges(final List<? extends DataNode> dataNodes) {
        final List<FialaBPExportEdgeFormat> edges = new ArrayList<>();
        long id = 1;
        for (DataNode dataNode : dataNodes) {
            final Optional<List<ArbitraryDataHolder>> opt = dataNode.getValue(METADATA_KEY_RELATIONSHIPS);
            if (opt.isEmpty()) {
                continue;
            }
            for (final ArbitraryDataHolder relationship : opt.get()) {
                final String stereotype = relationship.getValue(METADATA_KEY_STEREOTYPE, "");
                final Long from = relationship.getValue(METADATA_KEY_FROM, -1L);
                final Long to = relationship.getValue(METADATA_KEY_TO, -1L);
                final String name = relationship.getValue(METADATA_KEY_NAME, "");
                edges.add(new FialaBPExportEdgeFormat(id++, stereotype, from, to, name));
            }
        }
        return edges;
    }

    private List<FialaBPExportNodeFormat> getNodes(final List<? extends DataNode> dataNodes) {
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
                    try {
                        field.set(dataNodeFormat, value);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            nodes.add(dataNodeFormat);
        }

        // we need to remove the timezone from the value and keep the date before setting it if the date precision is "day"
        // this is due to Jackson removing the timezone and adding/subtracting the amount of hours
        // for example, if the time was 00:00 and the timezone was +1hr (i.e. 00:00:+01:00) or more, then the date gets rolled back to the previous one with 23:00 on the
        // clock (23:00:+00:00)
        // this is very likely unwanted because for birthdays we usually know the date, but not the exact hour, and so we default
        // the time of birth to 00:00 in the timezone of the state the person was born in
        // to solve this we basically add the time zone value and remove the timezone. the date is then recalculated internally in Calendar
        for (final FialaBPExportNodeFormat node : nodes) {
            stripTimezone(node.getBegin());
            stripTimezone(node.getEnd());
        }
        return nodes;
    }
}
