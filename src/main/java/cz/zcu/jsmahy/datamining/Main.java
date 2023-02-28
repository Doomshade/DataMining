package cz.zcu.jsmahy.datamining;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The entry point of the application.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class Main extends Application {
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

    private void extractResourcesToTempFolder() throws IOException, URISyntaxException {
        //If folder exist, delete it.
        String destPath = "temp/";

        final File file = new File(Main.class.getProtectionDomain()
                                             .getCodeSource()
                                             .getLocation()
                                             .toURI());
        JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> enums = jarFile.entries();
        while (enums.hasMoreElements()) {
            JarEntry entry = enums.nextElement();
            if (entry.getName()
                     .startsWith("resources")) {
                File toWrite = new File(destPath + entry.getName());
                if (entry.isDirectory()) {
                    toWrite.mkdirs();
                    continue;
                }
                InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(toWrite));
                byte[] buffer = new byte[2048];
                for (; ; ) {
                    int nBytes = in.read(buffer);
                    if (nBytes <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nBytes);
                }
                out.flush();
                out.close();
                in.close();
            }
            System.out.println(entry.getName());
        }
    }

    @Override
    public void start(final Stage stage) throws Exception {
        Main.stage = stage;
        // unpack the jar
        try {
            extractResourcesToTempFolder();
        } catch (IOException | URISyntaxException e) {
            LOGGER.throwing(e);
        }
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
        stage.setTitle(resourceBundle.getString("stage-title"));
        final Scene scene = SCENE_MANAGER.getScene(FXMLScene.MAIN);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
