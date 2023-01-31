package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

import java.io.*;

public abstract class ExportTask extends Task<Void> implements Closeable {
    protected final Writer out;

    protected ExportTask(final OutputStream out) {
        this.out = new OutputStreamWriter(out);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    // helper methods for Java's amazing I/O API
    protected final PrintWriter printWriter(final OutputStream out) {
        return new PrintWriter(out);
    }

    protected final PrintWriter printWriter(final Writer out) {
        return new PrintWriter(out);
    }

    protected final BufferedWriter buffered(final Writer out) {
        return new BufferedWriter(out);
    }

    protected final BufferedOutputStream buffered(final OutputStream out) {
        return new BufferedOutputStream(out);
    }
}
