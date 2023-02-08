package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

/**
 * <p>A {@link Task} for serializing the whole {@link DataNode} tree.</p>
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public abstract class DataNodeSerializerTask extends Task<Void> {
    protected final OutputStream out;
    protected final DataNode root;

    /**
     * @param out  the output stream to write to
     * @param root the data node root
     */
    public DataNodeSerializerTask(final OutputStream out, final DataNode root) {
        this.out = requireNonNull(out);
        this.root = requireNonNull(root);
        if (!root.isRoot()) {
            throw new IllegalArgumentException("The data node must be a root.");
        }
    }
}
