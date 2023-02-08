package cz.zcu.jsmahy.datamining.export;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeDeserializerTask;
import cz.zcu.jsmahy.datamining.api.DataNodeSerializer;
import cz.zcu.jsmahy.datamining.api.DataNodeSerializerTask;

import java.io.InputStream;
import java.io.OutputStream;

public class FialaBPSerializer implements DataNodeSerializer {
    @Override
    public DataNodeSerializerTask createSerializerTask(final OutputStream out, final DataNode root) {
        return new FialaBPSerializerTask(out, root);
    }

    @Override
    public DataNodeDeserializerTask createDeserializerTask(final InputStream in) {
        return new FialaBPDeserializerTask(in);
    }

    @Override
    public String getFileExtension() {
        return "js";
    }


}
