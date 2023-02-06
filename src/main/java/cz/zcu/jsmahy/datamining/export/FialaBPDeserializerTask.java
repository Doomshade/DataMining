package cz.zcu.jsmahy.datamining.export;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeDeserializerTask;

import java.io.InputStream;

class FialaBPDeserializerTask extends DataNodeDeserializerTask {
    public FialaBPDeserializerTask(final InputStream in) {
        super(in);
    }

    @Override
    protected DataNode call() throws Exception {
        return null;
    }
}
