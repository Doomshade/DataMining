package cz.zcu.jsmahy.datamining.api;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;

public interface DataNodeSerializer {
    String DEFAULT_FILE_EXTENSION = "datanode";
    String FILE_NAME_FORMAT = "%s.%s";
    Logger LOGGER = LogManager.getLogger(DataNodeSerializer.class);

    private static void alertInvalidFileName(final AtomicReference<Response> ref, final String filename, final String lineName) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Soubor ''{0}'' má neplatné jméno. Přejmenujte prosím linii ''{1}''.", filename, lineName));
        final ButtonType result = alert.showAndWait()
                                       .orElse(ButtonType.OK);
        if (result == ButtonType.OK) {
            ref.set(Response.YES);
        } else {
            ref.set(Response.INVALID_RESPONSE);
        }
    }

    private static void alertFileExists(final AtomicReference<Response> ref, final String filename) {
        final Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Přejete si smazat {0}?", filename));
        final ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
        buttonTypes.clear();
        buttonTypes.addAll(ButtonType.YES, ButtonType.NO);
        final ButtonType result = alert.showAndWait()
                                       .orElse(ButtonType.NO);
        if (result == ButtonType.YES) {
            ref.set(Response.YES);
        } else if (result == ButtonType.NO) {
            ref.set(Response.NO);
        } else {
            ref.set(Response.INVALID_RESPONSE);
        }
    }

    /**
     * Exports the root.
     *
     * @param dataNodeRoot     the root
     * @param runningServices  the running services ref. if either ref is null no progress is added
     * @param finishedServices the finished services ref
     */
    default void exportRoot(final DataNode dataNodeRoot, @Nullable final ObservableList<Service<?>> runningServices, @Nullable final ObservableList<Service<?>> finishedServices) {
        // yeah... we actually don't really need it to run on the application thread
        // but this makes it much easier to build alerts with responses
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Root export must be called on JavaFX application thread but was: " + Thread.currentThread());
        }

        final OutputStream out;
        try {
            final String nodeName = dataNodeRoot.getValue(METADATA_KEY_NAME, "<no name>");
            String fileExtension = getFileExtension();
            if (fileExtension == null) {
                fileExtension = DEFAULT_FILE_EXTENSION;
            }
            final String filename = String.format(FILE_NAME_FORMAT, nodeName, fileExtension);

            // validate the file name
            try {
                Paths.get(filename);
            } catch (InvalidPathException ex) {
                final AtomicReference<Response> ref = new AtomicReference<>();
                alertInvalidFileName(ref, filename, nodeName);
                if (ref.get() == Response.INVALID_RESPONSE) {
                    LOGGER.error("Invalid response received.");
                }
                return;
            }

            final File file = new File(filename);
            if (file.exists()) {
                // residuum after some multithreading madness
                final AtomicReference<Response> ref = new AtomicReference<>();
                alertFileExists(ref, filename);

                final Response response = ref.get();
                if (response == Response.NO) {
                    return;
                }
                if (response == Response.INVALID_RESPONSE) {
                    LOGGER.error("Invalid response received.");
                    return;
                }
                assert response == Response.YES;
            }

            //noinspection resource
            out = new FileOutputStream(filename);
        } catch (FileNotFoundException ex) {
            throw new UncheckedIOException(ex);
        }
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
        dataNodeSerializationTask.setOnFailed(ev -> LOGGER.throwing(dataNodeSerializationTask.getException()));

        if (runningServices == null || finishedServices == null) {
            return;
        }
        dataNodeSerializationTask.setOnRunning(ev -> runningServices.add(dataNodeSerializationTask));
        dataNodeSerializationTask.setOnSucceeded(ev -> {
            finishedServices.add(dataNodeSerializationTask);
            runningServices.remove(dataNodeSerializationTask);
        });
        dataNodeSerializationTask.setOnCancelled(ev -> {
            finishedServices.add(dataNodeSerializationTask);
            runningServices.remove(dataNodeSerializationTask);
        });
        dataNodeSerializationTask.setOnFailed(ev -> {
            dataNodeSerializationTask.getOnFailed()
                                     .handle(ev);
            finishedServices.add(dataNodeSerializationTask);
            runningServices.remove(dataNodeSerializationTask);
        });
        dataNodeSerializationTask.start();
    }

    /**
     * Calls {@link #exportRoot(DataNode, ObservableList, ObservableList)} with both lists as {@code null}.
     *
     * @see #exportRoot(DataNode, ObservableList, ObservableList)
     */
    default void exportRoot(final DataNode dataNodeRoot) {
        exportRoot(dataNodeRoot, null, null);
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
     * @return The file extension. If this is {@code null} the extension defaults to {@link DataNodeSerializer#DEFAULT_FILE_EXTENSION}
     */
    String getFileExtension();

    enum Response {
        INVALID_RESPONSE,
        YES,
        NO
    }
}
