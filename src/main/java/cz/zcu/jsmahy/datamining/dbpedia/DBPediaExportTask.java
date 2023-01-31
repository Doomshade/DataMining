package cz.zcu.jsmahy.datamining.dbpedia;

import cz.zcu.jsmahy.datamining.api.ExportTask;

import java.io.OutputStream;
import java.io.PrintWriter;

public class DBPediaExportTask extends ExportTask {
    protected DBPediaExportTask(final OutputStream out) {
        super(out);
    }

    @Override
    protected Void call() throws Exception {
        PrintWriter pw = printWriter(buffered(out));
        return null;
    }
}
