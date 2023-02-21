package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.OutputStream;

public class JSONDataNodeSerializer implements DataNodeSerializer {
    private final BooleanProperty processedNodes = new SimpleBooleanProperty(false);
    private final ObjectMapper jsonObjectMapper;


    public JSONDataNodeSerializer(JSONDataNodeSerializationUtils utils) {
        this.jsonObjectMapper = utils.getJsonObjectMapper();
    }

    @Override
    public void serialize(final OutputStream out, final DataNode root) throws IOException {
        jsonObjectMapper.writeValue(out, root);
        processedNodes.set(true);
    }

    @Override
    public String getFileExtension() {
        return "json";
    }
}
