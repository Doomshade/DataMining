package cz.zcu.jsmahy.datamining.api;

import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

/**
 * Class responsible for serializing the {@link DataNode} tree.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public abstract class DataNodeSerializer<V> extends DataNodeExportTask<V> {
    protected final OutputStream out;
    protected final DataNode root;

    public DataNodeSerializer(final OutputStream out, final DataNode root) {
        this.out = requireNonNull(out);
        this.root = requireNonNull(root);
    }
}
