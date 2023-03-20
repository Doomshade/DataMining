package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

class JacksonDeserializer extends StdDeserializer<DataNode> {
    private final DataNodeFactory dataNodeFactory;

    public JacksonDeserializer(final DataNodeFactory dataNodeFactory) {
        super((Class<?>) null);
        this.dataNodeFactory = dataNodeFactory;
    }

    @Override
    public DataNode deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        if (dataNodeFactory == null) {
            return null;
        }

        final ObjectNode objectNode = p.getCodec()
                                       .readTree(p);
        final JsonNode idNode = objectNode.get("id");
        final long id;
        if (idNode instanceof NumericNode) {
            id = idNode.asLong();
        } else {
            throw new JsonParseException(p, "Could not find ID of a data node");
        }
        final JsonNode children = objectNode.get("children");
        return null;
    }
}
