package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class JSONDataNodeSerializationUtils {

    private final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    {
        JSON_OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JSON_OBJECT_MAPPER.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        JSON_OBJECT_MAPPER.enable(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL);
        JSON_OBJECT_MAPPER.enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
        JSON_OBJECT_MAPPER.setDateFormat(new StdDateFormat());
    }

    /**
     * @return A copy of the {@link ObjectMapper} instance.
     */
    public ObjectMapper getJsonObjectMapper() {
        return JSON_OBJECT_MAPPER.copy();
    }

}
