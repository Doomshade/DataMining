package cz.zcu.jsmahy.datamining;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;

/**
 * The entry point of the application.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class Main extends Application {
    public static final String TOP_LEVEL_FRONTEND_DIRECTORY_NAME = "frontend";
    public static final String TOP_LEVEL_RESOURCE_FRONTEND_DIRECTORY_NAME = "/".concat(Main.class.getPackage()
                                                                                                 .getName()
                                                                                                 .replaceAll("\\.", "/"))
                                                                               .concat("/")
                                                                               .concat(TOP_LEVEL_FRONTEND_DIRECTORY_NAME);
    public static final File TOP_LEVEL_FRONTEND_DIRECTORY = new File(TOP_LEVEL_FRONTEND_DIRECTORY_NAME);
    // no need to make this a singleton
    private static final SceneManager SCENE_MANAGER = new SceneManager();
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static Stage stage = null;

    public static void main(String[] args) throws ParseException {
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return stage;
    }

    @Override
    public void start(final Stage stage) throws Exception {
        Main.stage = stage;
        // TODO: Unpack the jar
//        try {
//            extractResourcesToTempFolder();
//        } catch (IOException | URISyntaxException e) {
//            LOGGER.throwing(e);
//        }
        // TODO: We presume the project is built with gradle, so the sources are unbundled
        // Copy the JS sources if they don't exist
        copyResources();
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
        stage.setTitle(resourceBundle.getString("stage-title"));
        final Scene scene = SCENE_MANAGER.getScene(FXMLScene.MAIN);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private void copyResources() throws URISyntaxException, IOException {
        final URL frontEndDirURL = getClass().getResource(TOP_LEVEL_RESOURCE_FRONTEND_DIRECTORY_NAME);
        if (frontEndDirURL == null) {
            throw new IOException("Could not find dir \"%s\" in resources.");
        }
        final File frontEndDir = new File(frontEndDirURL.toURI());
        if (!frontEndDir.isDirectory()) {
            throw new IOException("\"%s\" is not a directory.");
        }
        final File toplevelDir = new File(TOP_LEVEL_FRONTEND_DIRECTORY_NAME);
        if (!toplevelDir.isDirectory()) {
            if (!toplevelDir.mkdirs()) {
                throw new IOException("Failed to create directory " + TOP_LEVEL_FRONTEND_DIRECTORY_NAME);
            }
        }

        assert toplevelDir.isDirectory();
        assert frontEndDir.isDirectory();

        copyFolder(frontEndDir.toPath(), toplevelDir.toPath());
    }

    public void copyFolder(Path source, Path target, CopyOption... options) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)
                                                             .toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final Path newTarget = target.resolve(source.relativize(file)
                                                            .toString());
                if (newTarget.toFile()
                             .exists()) {
                    return FileVisitResult.CONTINUE;
                }
                Files.copy(file, newTarget, options);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
