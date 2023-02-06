package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

import java.io.InputStream;

/**
 * A {@link Task} for deserializing a {@link DataNode}.
 *
 * @param <V> {@inheritDoc}
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public abstract class DataNodeDeserializer<V> extends DataNodeExportTask<V> {
    protected final InputStream in;

    /**
     * @param in the input stream to read from
     */
    protected DataNodeDeserializer(final InputStream in) {
        this.in = in;
    }
}
