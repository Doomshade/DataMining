package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;
import static cz.zcu.jsmahy.datamining.util.Alerts.*;

public class JSONDataNodeSerializer implements DataNodeSerializer {
    public static final File EXPORT_FOLDER = new File("export");
    private final BooleanProperty processedNodes = new SimpleBooleanProperty(false);
    private final ObjectMapper jsonObjectMapper;

    @Inject
    public JSONDataNodeSerializer(JSONDataNodeSerializationUtils utils) {
        this.jsonObjectMapper = utils.getJsonObjectMapper();
    }

    /**
     * Exports the root.
     *
     * @param dataNodeRoot     the root
     * @param runningServices  the running services ref. if either ref is null no progress is added
     * @param finishedServices the finished services ref
     * @param failedNodes      the failed services
     */
    @Override
    public void exportRoot(final DataNode dataNodeRoot,
                           @Nullable final ObservableList<Service<?>> runningServices,
                           @Nullable final ObservableList<Service<?>> finishedServices,
                           @Nullable final ObservableList<DataNode> failedNodes) throws IOException {
        // yeah... we actually don't really need it to run on the application thread
        // but this makes it much easier to build alerts with responses
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Root export must be called on JavaFX application thread but was: " + Thread.currentThread());
        }

        File folder = EXPORT_FOLDER;
        if (!folder.isDirectory()) {
            if (!folder.mkdirs()) {
                // we could not create the export folder, try to get the current working directory
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

        // get the file name
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
            final AtomicReference<SerializationResponse> ref = new AtomicReference<>();
            alertInvalidFileName(ref, filename, nodeName);
            if (ref.get() == SerializationResponse.INVALID_RESPONSE) {
                LOGGER.error("Invalid response received.");
            }
            return;
        }

        // create the file and export it
        final File file = new File(folder, filename);
        if (file.exists()) {
            // residuum after some multithreading madness
            final AtomicReference<SerializationResponse> ref = new AtomicReference<>();
            alertFileExists(ref, file.getPath());

            final SerializationResponse response = ref.get();
            if (response == SerializationResponse.NO) {
                return;
            }
            if (response == SerializationResponse.INVALID_RESPONSE) {
                LOGGER.error("Invalid response received.");
                return;
            }
            assert response == SerializationResponse.YES;
        }
        exportRoot(file, dataNodeRoot, runningServices, finishedServices, failedNodes);
    }

    @Override
    public void serialize(final OutputStream out, final DataNode root) throws IOException {
        jsonObjectMapper.writeValue(out, root);
        processedNodes.set(true);
    }

    @Override
    public String getFileExtension() {
        return "json";
    }
}
