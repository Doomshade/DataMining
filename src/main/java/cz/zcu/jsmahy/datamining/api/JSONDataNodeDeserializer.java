package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JSONDataNodeDeserializer implements DataNodeDeserializer {
    private final ObjectMapper jsonObjectMapper;

    @Inject
    public JSONDataNodeDeserializer(JSONDataNodeSerializationUtils utils) {
        this.jsonObjectMapper = utils.getJsonObjectMapper();
    }

    private void setupParents(final DataNode parent) {
        for (DataNode dataNode : parent) {
            if (dataNode instanceof DataNodeImpl dataNodeImpl) {
                dataNodeImpl.setParent(parent);
            }
            setupParents(dataNode);
        }
    }

    @Override
    public String[] getAcceptedFileExtensions() {
        return new String[] {"json"};
    }

    @Override
    public DataNode deserialize(final InputStream in) throws IOException {
        final DataNode root = jsonObjectMapper.readValue(new InputStreamReader(in), DataNode.class);
        // we don't store the parent in serialization because we would have infinite recursion calls
        // the data node already stores the children, and when serializing the children
        // the serializer tries to serialize the parent, and that tries to serialize the children
        // the serializer tries to serialize the parent of the children, and then it tries to serialize the children of the parent
        // children (0) -> parent -> children (0) -> parent -> children (0) -> parent -> (...)
        setupParents(root);
        return root;
    }
}
