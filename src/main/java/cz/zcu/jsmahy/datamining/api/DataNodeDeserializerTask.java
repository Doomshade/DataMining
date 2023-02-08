package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * <p>A {@link Task} for deserializing a {@link DataNode}.</p>
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public abstract class DataNodeDeserializerTask extends Task<DataNode> {
    protected final InputStream in;
    protected final DataNodeFactory dataNodeFactory;

    /**
     * @param in              the input stream to read from
     * @param dataNodeFactory
     */
    protected DataNodeDeserializerTask(final InputStream in, final DataNodeFactory dataNodeFactory) {
        this.in = requireNonNull(in);
        this.dataNodeFactory = requireNonNull(dataNodeFactory);
    }

    protected void panicInvalidDataFormat(String expected) throws IOException {
        throw new IOException("Invalid data format. Expected: ".concat(expected));
    }

}
