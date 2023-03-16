package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class JSONDataNodeSerializationUtils {

    private final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    {
        JSON_OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JSON_OBJECT_MAPPER.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        JSON_OBJECT_MAPPER.enable(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL);
        JSON_OBJECT_MAPPER.enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
        JSON_OBJECT_MAPPER.setDateFormat(new StdDateFormat());
        final SimpleModule module = new SimpleModule();
        module.addSerializer(RDFNode.class, new RDFNodeSerialization.RDFNodeSerializer<>());
        module.addDeserializer(RDFNode.class, new RDFNodeSerialization.RDFNodeDeserializer<>());
        module.addSerializer(ResourceImpl.class, new RDFNodeSerialization.RDFNodeSerializer<>());
        module.addDeserializer(ResourceImpl.class, new RDFNodeSerialization.RDFNodeDeserializer<>());
        module.addSerializer(LiteralImpl.class, new RDFNodeSerialization.RDFNodeSerializer<>());
        module.addDeserializer(LiteralImpl.class, new RDFNodeSerialization.RDFNodeDeserializer<>());
        JSON_OBJECT_MAPPER.registerModule(module);
    }

    /**
     * @return A copy of the {@link ObjectMapper} instance.
     */
    public ObjectMapper getJsonObjectMapper() {
        return JSON_OBJECT_MAPPER.copy();
    }

}
