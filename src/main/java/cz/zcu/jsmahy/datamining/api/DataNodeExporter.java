package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import static java.util.Objects.requireNonNull;

/**
 * Task to export the data in specified export format. Supports
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DataNodeExporter extends Service<Void> {
    private final DataNodeExportTask<Void> exportTask;

    public DataNodeExporter(final DataNodeExportTask<Void> exportTask) {
        if (!(exportTask instanceof DataNodeSerializer<Void>) && !(exportTask instanceof DataNodeDeserializer<Void>)) {
            throw new IllegalArgumentException(String.format("The data node task must either be of type %s or %s", DataNodeSerializer.class, DataNodeDeserializer.class));
        }
        this.exportTask = requireNonNull(exportTask);
    }

    @Override
    protected Task<Void> createTask() {
        return exportTask;
    }
}
