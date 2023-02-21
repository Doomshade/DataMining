package cz.zcu.jsmahy.datamining.api;

import java.io.IOException;
import java.io.OutputStream;

public interface DataNodeSerializer {
    String DEFAULT_FILE_EXTENSION = "datanode";

    /**
     * <p>Serializes the whole {@link DataNode} tree.</p>
     *
     * @throws IOException if the {@link DataNode} tree failed to serialize
     * @author Jakub Šmrha
     * @since 1.0
     */
    void serialize(OutputStream out, DataNode root) throws IOException;

    /**
     * @return The file extension. If this is {@code null} the extension defaults to {@link DataNodeSerializer#DEFAULT_FILE_EXTENSION}
     */
    String getFileExtension();
}
