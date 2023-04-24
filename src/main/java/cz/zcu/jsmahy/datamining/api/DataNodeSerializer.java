package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static cz.zcu.jsmahy.datamining.util.Alerts.alertExportFailed;
import static cz.zcu.jsmahy.datamining.util.Alerts.alertExportSuccess;

/**
 * The {@link DataNode} serializer. A default JSON serialization implementation already exists: {@link JSONDataNodeSerializer}. For implementation details see that class.
 *
 * @author Jakub Šmrha
 * @see JSONDataNodeSerializer
 * @since 1.0
 */
public interface DataNodeSerializer {
    String DEFAULT_FILE_EXTENSION = "datanode";
    String FILE_NAME_FORMAT = "%s.%s";
    Logger LOGGER = LogManager.getLogger(DataNodeSerializer.class);

    void exportRoot(DataNode dataNodeRoot, @Nullable ObservableList<Service<?>> runningServices, @Nullable ObservableList<Service<?>> finishedServices,
                    @Nullable ObservableList<DataNode> failedNodes) throws IOException;

    /**
     * Exports the data node root to the target file. Updates the lists as the data is exported (such as started exporting, finished exporting, and failed exporting). Note that a data node that failed
     * to export will be added to the finished services.
     *
     * @param targetFile       the file to export to
     * @param dataNodeRoot     the root data node to export
     * @param runningServices  the export {@link Service} is added to this list once the export starts
     * @param finishedServices the export {@link Service} is added to this list once the export ends
     * @param failedNodes      the root data node is added to this list once the export fails
     */
    default void exportRoot(File targetFile,
                            DataNode dataNodeRoot,
                            @Nullable ObservableList<Service<?>> runningServices,
                            @Nullable ObservableList<Service<?>> finishedServices,
                            @Nullable ObservableList<DataNode> failedNodes) throws IOException {
        // noinspection resource
        final OutputStream out = new FileOutputStream(targetFile);
        final Service<Void> dataNodeSerializationTask = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        serialize(out, dataNodeRoot);
                        return null;
                    }
                };
            }
        };
        dataNodeSerializationTask.setOnFailed(ev -> {
            try {
                out.close();
            } catch (IOException e) {
                LOGGER.throwing(e);
            }
            LOGGER.throwing(dataNodeSerializationTask.getException());
            alertExportFailed(dataNodeRoot, dataNodeSerializationTask.getException());
        });

        if (runningServices == null || finishedServices == null || failedNodes == null) {
            dataNodeSerializationTask.setOnSucceeded(ev -> {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.throwing(e);
                }
                alertExportSuccess(dataNodeRoot, targetFile);
            });
        } else {
            dataNodeSerializationTask.setOnRunning(ev -> runningServices.add(dataNodeSerializationTask));
            dataNodeSerializationTask.setOnSucceeded(ev -> {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.throwing(e);
                }
                finishedServices.add(dataNodeSerializationTask);
                runningServices.remove(dataNodeSerializationTask);
            });
            dataNodeSerializationTask.setOnCancelled(ev -> {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.throwing(e);
                }
                alertExportFailed(dataNodeRoot);
                finishedServices.add(dataNodeSerializationTask);
                runningServices.remove(dataNodeSerializationTask);
            });
            dataNodeSerializationTask.setOnFailed(ev -> {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.throwing(e);
                }
                LOGGER.throwing(dataNodeSerializationTask.getException());
                alertExportFailed(dataNodeRoot, dataNodeSerializationTask.getException());
                failedNodes.add(dataNodeRoot);
                finishedServices.add(dataNodeSerializationTask);
                runningServices.remove(dataNodeSerializationTask);
            });
        }
        dataNodeSerializationTask.start();
    }

    /**
     * Calls {@link #exportRoot(DataNode, ObservableList, ObservableList, ObservableList)} with all lists as {@code null}.
     *
     * @see #exportRoot(DataNode, ObservableList, ObservableList, ObservableList)
     */
    default void exportRoot(final DataNode dataNodeRoot) throws IOException {
        exportRoot(dataNodeRoot, null, null, null);
    }

    /**
     * <p>Serializes the whole {@link DataNode} tree.</p>
     *
     * @throws IOException if the {@link DataNode} tree failed to serialize
     * @author Jakub Šmrha
     * @since 1.0
     */
    void serialize(OutputStream out, DataNode root) throws IOException;

    /**
     * @return The file extension. If this is {@code null} you may default the extension to {@link DataNodeSerializer#DEFAULT_FILE_EXTENSION}.
     */
    String getFileExtension();

}
