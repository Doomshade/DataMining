package cz.zcu.jsmahy.datamining.api;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO: javadoc
 */
public interface DataNodeSerializer {
    DataNodeSerializerTask createSerializerTask(OutputStream out, DataNode root);

    DataNodeDeserializerTask createDeserializerTask(InputStream in);

    String getFileExtension();
}
