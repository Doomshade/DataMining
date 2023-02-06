package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

import java.io.InputStream;

/**
 * A {@link Task} for deserializing a {@link DataNode}.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public abstract class DataNodeDeserializerTask extends DataNodeExportTask<DataNode> {
    protected final InputStream in;

    /**
     * @param in the input stream to read from
     */
    protected DataNodeDeserializerTask(final InputStream in) {
        this.in = in;
    }
}
