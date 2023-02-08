package cz.zcu.jsmahy.datamining.api;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is responsible for serialization of a {@link DataNode}. It provides two tasks, one for serialization {@link #createSerializerTask(OutputStream, DataNode)}, and one for deserialization
 * {@link #createDeserializerTask(InputStream)}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface DataNodeSerializer {
    /**
     * Creates a new serialization task.
     *
     * @param out  the output stream of the serialization
     * @param root the data node root to serialize
     *
     * @return a task that performs serialization
     */
    DataNodeSerializerTask createSerializerTask(OutputStream out, DataNode root);

    /**
     * Creates a new deserialization task.
     *
     * @param in the input stream of the deserialization
     *
     * @return a task that performs deserialization
     */
    DataNodeDeserializerTask createDeserializerTask(InputStream in);

    /**
     * @return the file extension of the serialized {@link DataNode}
     */
    String getFileExtension();
}
