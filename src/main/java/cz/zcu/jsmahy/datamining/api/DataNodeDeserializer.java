package cz.zcu.jsmahy.datamining.api;

import java.io.InputStream;

public abstract class DataNodeDeserializer<V> extends DataNodeExportTask<V> {
    protected final InputStream in;

    protected DataNodeDeserializer(final InputStream in) {
        this.in = in;
    }
}
