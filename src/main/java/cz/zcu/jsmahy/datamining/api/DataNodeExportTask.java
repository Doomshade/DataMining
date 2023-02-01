package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

import java.io.*;

/**
 * The task <b>MUST</b> be stateless. That means, each call to the method {@link Task#call()} <b>MUST NOT</b> modify the object's state. This is required as only one instance of this task is created
 * and can be reused.
 *
 * @param <V> The {@link Task} generic argument
 */
public abstract class DataNodeExportTask<V> extends Task<V> {
    protected PrintWriter printWriter(Writer out) {
        return new PrintWriter(out);
    }

    protected PrintWriter printWriter(OutputStream out) {
        return new PrintWriter(out);
    }

    protected BufferedWriter buffered(Writer out) {
        return new BufferedWriter(out);
    }

    protected BufferedOutputStream buffered(OutputStream out) {
        return new BufferedOutputStream(out);
    }
}
