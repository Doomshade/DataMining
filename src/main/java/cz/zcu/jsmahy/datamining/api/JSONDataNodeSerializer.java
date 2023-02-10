package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.google.inject.Inject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class JSONDataNodeSerializer implements DataNodeSerializer {

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    static {
        JSON_OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JSON_OBJECT_MAPPER.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        JSON_OBJECT_MAPPER.enable(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL);
        JSON_OBJECT_MAPPER.enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
        JSON_OBJECT_MAPPER.setDateFormat(new StdDateFormat());
    }

    private final DataNodeFactory dataNodeFactory;

    @Inject
    public JSONDataNodeSerializer(final DataNodeFactory dataNodeFactory) {
        this.dataNodeFactory = dataNodeFactory;
    }

    /**
     * @return A copy of the {@link ObjectMapper} instance.
     */
    public static ObjectMapper getJsonObjectMapper() {
        return JSON_OBJECT_MAPPER.copy();
    }

    @Override
    public DataNodeSerializerTask createSerializerTask(final OutputStream out, final DataNode root) {
        return new JSONDataNodeSerializerTask(out, root);
    }

    @Override
    public DataNodeDeserializerTask createDeserializerTask(final InputStream in) {
        return new JSONDataNodeDeserializerTask(in, dataNodeFactory);
    }

    @Override
    public String getFileExtension() {
        return "json";
    }

    public static class JSONDataNodeDeserializerTask extends DataNodeDeserializerTask {

        /**
         * @param in              the input stream to read from
         * @param dataNodeFactory
         */
        protected JSONDataNodeDeserializerTask(final InputStream in, final DataNodeFactory dataNodeFactory) {
            super(in, dataNodeFactory);
        }

        @Override
        protected DataNode call() throws Exception {
            final DataNode root = JSON_OBJECT_MAPPER.readValue(new InputStreamReader(in), DataNode.class);
            // we don't store the parent in serialization because we would have infinite recursion calls
            // the data node already stores the children, and when serializing the children
            // the serializer tries to serialize the parent, and that tries to serialize the children
            // the serializer tries to serialize the parent of the children, and then it tries to serialize the children of the parent
            // children (0) -> parent -> children (0) -> parent -> children (0) -> parent -> (...)
            setupParents(root);
            return root;
        }

        private void setupParents(final DataNode parent) {
            for (DataNode dataNode : parent) {
                if (dataNode instanceof DataNodeImpl dataNodeImpl) {
                    dataNodeImpl.setParent(parent);
                }
                setupParents(dataNode);
            }
        }
    }

    public static class JSONDataNodeSerializerTask extends DataNodeSerializerTask {

        /**
         * @param out  the output stream to write to
         * @param root the data node root
         */
        public JSONDataNodeSerializerTask(final OutputStream out, final DataNode root) {
            super(out, root);
        }

        @Override
        protected Void call() throws Exception {
            JSON_OBJECT_MAPPER.writeValue(out, root);
            return null;
        }
    }
}
