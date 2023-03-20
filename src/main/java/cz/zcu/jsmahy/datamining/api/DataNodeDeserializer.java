package cz.zcu.jsmahy.datamining.api;

import java.io.IOException;
import java.io.InputStream;

/**
 * The {@link DataNode} deserializer. For now, we only permit the built-in JSON deserializer as there is no reason to deserialize one's own serialized data.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public sealed interface DataNodeDeserializer permits JSONDataNodeDeserializer {
    /**
     * @return The file extensions this deserializers accept.
     */
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
