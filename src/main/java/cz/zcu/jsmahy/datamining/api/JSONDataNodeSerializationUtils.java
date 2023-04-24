package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.google.inject.Inject;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Utilities around JSON {@link DataNode} serialization.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class JSONDataNodeSerializationUtils {

    public static final Version VERSION = new Version(0, 1, 0, "", "cz.zcu.jsmahy", "DataMining");
    private final ObjectMapper jsonObjectMapper = new ObjectMapper();

    @Inject
    public JSONDataNodeSerializationUtils(DataNodeFactory dataNodeFactory) {
        jsonObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        jsonObjectMapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        jsonObjectMapper.enable(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL);
        jsonObjectMapper.enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
        jsonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        jsonObjectMapper.setDateFormat(new StdDateFormat());

        final SimpleModule module = new SimpleModule("builtin");
        module.addSerializer(RDFNode.class, new RDFNodeSerialization.RDFNodeSerializer<>());
        module.addDeserializer(RDFNode.class, new RDFNodeSerialization.RDFNodeDeserializer<>());
//        module.addDeserializer(DataNode.class, new JacksonDeserializer(dataNodeFactory));
        jsonObjectMapper.registerModule(module);
    }

    /**
     * @return A copy of the {@link ObjectMapper} instance.
     */
    public ObjectMapper getJsonObjectMapper() {
        return jsonObjectMapper.copy();
    }

}
