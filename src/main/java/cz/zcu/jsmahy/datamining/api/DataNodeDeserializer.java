package cz.zcu.jsmahy.datamining.api;

import java.io.IOException;
import java.io.InputStream;

public interface DataNodeDeserializer {
    String[] getAcceptedFileExtensions();

    /**
     * Deserializes the {@link DataNode} tree from input stream.
     *
     * @param in the input stream
     *
     * @return The {@link DataNode} root
     */
    DataNode deserialize(InputStream in) throws IOException;
}
