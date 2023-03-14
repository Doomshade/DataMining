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

    private static void alertFileExists(final AtomicReference<Response> ref, final String lineName) {
        final Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Přejete si přepsat ''{0}''?", lineName));
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

    private static void alertExportSuccess(final DataNode dataNodeRoot, final File exportedFile) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Úspěšně exportováno ''{0}''. Soubor: ''{1}''", dataNodeRoot.getValue(METADATA_KEY_NAME, "<no name>"), exportedFile));
        alert.show();
    }

    private static void alertExportFailed(final DataNode dataNodeRoot) {
        alertExportFailed(dataNodeRoot, null);
    }

    private static void alertExportFailed(final DataNode dataNodeRoot, final Throwable throwable) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Nepodařilo se exportovat ''{0}''.", dataNodeRoot.getValue(METADATA_KEY_NAME, "<no name>")));
        if (throwable != null) {
            alert.setContentText(alert.getContentText()
                                      .concat("Výjimka: \n" + throwable));
        }
        alert.show();
    }

    private static void alertCouldNotCreateFolder(final File folder) {
        final Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Nepodařilo se vytvořit adresář ''{0}'' (bez výjimky)", folder));
        alert.showAndWait();
    }

    private static void alertCouldNotGetCwd(final File folder) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Nepodařilo se vybrat aktuální adresář ''{0}''. Něco je špatně...", folder));
        alert.showAndWait();
    }

    default void exportRoot(final File targetFile, final DataNode dataNodeRoot) {
        exportRoot(targetFile, dataNodeRoot, null, null, null);
    }

    /**
     * Exports the root.
     *
     * @param dataNodeRoot     the root
     * @param runningServices  the running services ref. if either ref is null no progress is added
     * @param finishedServices the finished services ref
     * @param failedNodes      the failed services
     */
    default void exportRoot(final DataNode dataNodeRoot, @Nullable final ObservableList<Service<?>> runningServices,
                            @Nullable final ObservableList<Service<?>> finishedServices, @Nullable final ObservableList<DataNode> failedNodes) {
        // yeah... we actually don't really need it to run on the application thread
        // but this makes it much easier to build alerts with responses
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Root export must be called on JavaFX application thread but was: " + Thread.currentThread());
        }

        File folder = new File("export");
        if (!folder.isDirectory()) {
            if (!folder.mkdirs()) {
                alertCouldNotCreateFolder(folder);
                try {
                    folder = Paths.get("")
                                  .toFile();
                } catch (Exception ex) {
                    alertCouldNotGetCwd(folder);
                    Platform.exit();
                    System.exit(1);
                    throw new RuntimeException(ex);
                }
            }
        }

        final File file;
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

        file = new File(folder, filename);
        if (file.exists()) {
            // residuum after some multithreading madness
            final AtomicReference<Response> ref = new AtomicReference<>();
            alertFileExists(ref, nodeName);

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
        exportRoot(file, dataNodeRoot, runningServices, finishedServices, failedNodes);
    }

    default void exportRoot(final File targetFile, final DataNode dataNodeRoot, final @Nullable ObservableList<Service<?>> runningServices,
                            final @Nullable ObservableList<Service<?>> finishedServices, final @Nullable ObservableList<DataNode> failedNodes) {
        final OutputStream out;
        try {
            // noinspection resource
            out = new FileOutputStream(targetFile);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
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
                dataNodeSerializationTask.getOnFailed()
                                         .handle(ev);
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
    default void exportRoot(final DataNode dataNodeRoot) {
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

    enum Response {
        INVALID_RESPONSE,
        YES,
        NO
    }
}
