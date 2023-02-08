package cz.zcu.jsmahy.datamining.export;

import cz.zcu.jsmahy.datamining.api.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_RELATIONSHIPS;
import static cz.zcu.jsmahy.datamining.export.FialaBPSerializer.PREFIX;
import static cz.zcu.jsmahy.datamining.export.FialaBPSerializer.SUFFIX;

class FialaBPDeserializerTask extends DataNodeDeserializerTask {
    public FialaBPDeserializerTask(final InputStream in, final DataNodeFactory dataNodeFactory) {
        super(in, dataNodeFactory);
    }

    @Override
    protected DataNode call() throws Exception {
        // we don't allow formatted JSON because we lazy to make a lexer/parser :)
        // just assume the data file is not going to be modified
        final String prefix = new String(in.readNBytes(PREFIX.length()), StandardCharsets.UTF_8);
        if (!prefix.equals(PREFIX)) {
            panicInvalidDataFormat(PREFIX + " at the start of the file");
        }
        final String bodyAndSuffix = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        if (!bodyAndSuffix.endsWith(SUFFIX)) {
            panicInvalidDataFormat(SUFFIX + " at the end of the file");
        }
        // body is the json
        final String body = bodyAndSuffix.substring(0, bodyAndSuffix.length() - SUFFIX.length());
        final FialaBPExportFormatRoot bpFormatRoot = JSONDataNodeSerializer.JSON_OBJECT_MAPPER.readValue(body, FialaBPExportFormatRoot.class);
        final Field[] nodeFields = FialaBPExportNodeFormat.class.getDeclaredFields();
        for (Field field : nodeFields) {
            field.trySetAccessible();
        }

        final DataNode root = dataNodeFactory.newRoot("Imported root");
        for (final FialaBPExportNodeFormat node : bpFormatRoot.getNodes()) {
            final DataNode dataNode = dataNodeFactory.newNode(root, node.getName());
            for (Field nodeField : nodeFields) {
                dataNode.addMetadata(nodeField.getName(), nodeField.get(node));
            }
        }

        final Field[] edgeFields = FialaBPExportEdgeFormat.class.getDeclaredFields();
        for (Field field : edgeFields) {
            field.trySetAccessible();
        }
        for (final FialaBPExportEdgeFormat edge : bpFormatRoot.getEdges()) {
            final Optional<? extends DataNode> targetNode = root.getChildren()
                                                                .stream()
                                                                .filter(x -> x.getValue("id", -1) == edge.getFrom())
                                                                .findAny();
            assert targetNode.isPresent(); // if it's not present some implementation changed

            final DataNode dataNode = targetNode.get();
            final List<ArbitraryDataHolder> relationships = dataNode.getValue(METADATA_KEY_RELATIONSHIPS, new ArrayList<>());

            final ArbitraryDataHolder relationship = new DefaultArbitraryDataHolder();
            for (final Field field : edgeFields) {
                relationship.addMetadata(field.getName(), field.get(edge));
            }
            relationships.add(relationship);

            if (!dataNode.hasMetadataKey(METADATA_KEY_RELATIONSHIPS)) {
                dataNode.addMetadata(METADATA_KEY_RELATIONSHIPS, relationships);
            }
        }
        return root;
    }
}
