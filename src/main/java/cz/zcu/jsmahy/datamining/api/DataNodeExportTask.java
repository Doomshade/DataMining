package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

import java.io.*;

/**
 * The task <b>MUST</b> be stateless. That means, each call to the method {@link Task#call()} <b>MUST NOT</b> modify the object's state. This is required as only one instance of this task is created
 * and can be reused.
 */
abstract class DataNodeExportTask<V> extends Task<V> {
    protected static PrintWriter printWriter(Writer out) {
        return new PrintWriter(out);
    }

    protected static PrintWriter printWriter(OutputStream out) {
        return new PrintWriter(out);
    }

    protected static BufferedWriter buffered(Writer out) {
        return new BufferedWriter(out);
    }

    protected static BufferedOutputStream buffered(OutputStream out) {
        return new BufferedOutputStream(out);
    }
}
