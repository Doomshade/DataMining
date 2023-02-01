package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

import java.io.*;

import static java.util.Objects.requireNonNull;

public class ExportTask extends Task<String> implements Closeable, Flushable {
    protected final Writer out;
    protected final DataNode root;

    protected ExportTask(final OutputStream out, final DataNode root, final ExportFormat exportFormat) {
        this.out = new OutputStreamWriter(requireNonNull(out));
        this.root = requireNonNull(root);
        if (!root.isRoot()) {
            throw new IllegalArgumentException("The data node must be root. Provided: " + root);
        }
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
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

    @Override
    protected String call() throws Exception {
        return null;
    }
}
