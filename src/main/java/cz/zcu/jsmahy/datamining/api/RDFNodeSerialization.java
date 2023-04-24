package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.io.IOException;

/**
 * RDFNode serialization via Jackson.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public final class RDFNodeSerialization {

    public static final String FIELDNAME_TYPE = "type";
    public static final String FIELDNAME_DATA = "data";
    public static final String RDFNODE_TYPE_RESOURCE = "resource";
    public static final String RDFNODE_TYPE_LITERAL = "literal";

    public static class RDFNodeSerializer<T extends RDFNode> extends StdSerializer<T> {

        public RDFNodeSerializer() {
            this(null);
        }

        protected RDFNodeSerializer(final Class<T> t) {
            super(t);
        }

        @Override
        public void serialize(final T value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
            final String rdfNodeType = value instanceof Resource ? RDFNODE_TYPE_RESOURCE : value instanceof Literal ? RDFNODE_TYPE_LITERAL : "";
            if (rdfNodeType.isEmpty()) {
                throw new IllegalStateException("RDFNode is not a Resource nor a Literal.");
            }

            gen.writeStartObject();
            gen.writeStringField(FIELDNAME_TYPE, rdfNodeType);
            if (value instanceof Resource) {
                gen.writeObjectField(FIELDNAME_DATA,
                                     value.asResource()
                                          .getURI());
            } else {
                gen.writeObjectField(FIELDNAME_DATA,
                                     value.asLiteral()
                                          .getValue());
            }
            gen.writeEndObject();
        }
    }

    public static class RDFNodeDeserializer<T extends RDFNode> extends StdDeserializer<T> {

        public RDFNodeDeserializer() {
            this(null);
        }

        protected RDFNodeDeserializer(final Class<?> vc) {
            super(vc);
        }

        @Override
        public T deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            final JsonNode node = jp.getCodec()
                                    .readTree(jp);
            final String rdfNodeType = node.get(FIELDNAME_TYPE)
                                           .asText();
            final JsonNode rdfNodeData = node.get(FIELDNAME_DATA);
            if (rdfNodeType.equals(RDFNODE_TYPE_RESOURCE)) {
                return (T) ResourceFactory.createResource(rdfNodeData.asText());
            } else if (rdfNodeType.equals(RDFNODE_TYPE_LITERAL)) {
                return (T) ResourceFactory.createTypedLiteral(((POJONode) rdfNodeData).getPojo());
            }
            throw new IllegalStateException(String.format("Invalid RDFNode type received. Expected: %s, %s", RDFNODE_TYPE_LITERAL, RDFNODE_TYPE_RESOURCE));
        }
    }
}
