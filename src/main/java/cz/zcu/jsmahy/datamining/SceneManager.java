package cz.zcu.jsmahy.datamining;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cz.zcu.jsmahy.datamining.api.DataMiningModule;
import cz.zcu.jsmahy.datamining.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.export.FialaBPModule;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
final class SceneManager {
    public static final String FXML_PATH = "/fxml/";
    private static final Logger LOGGER = LogManager.getLogger(SceneManager.class);
    private static final String FXML_SUFFIX = ".fxml";
    private static final Module[] MODULES = new Module[] {
            new DataMiningModule(), new DBPediaModule(), new FialaBPModule()
    };

    private static final Injector injector;

    static {
        injector = Guice.createInjector(MODULES);
    }

    public Scene getScene(final FXMLScene fxmlScene) throws IOException {
        String fxmlScenePath = fxmlScene.getScenePath();
        final int fxmlSuffixIndex = fxmlScenePath.lastIndexOf(FXML_SUFFIX);
        if (fxmlSuffixIndex >= 0) {
            LOGGER.warn("FXML Scene {} has concatenated \".fxml\". This is not needed", fxmlScene.name());
            fxmlScenePath = fxmlScenePath.substring(0, fxmlSuffixIndex);
        }

        final InputStream fxml = Main.class.getResourceAsStream(FXML_PATH.concat(fxmlScenePath)
                                                                         .concat(FXML_SUFFIX));
        if (fxml == null) {
            throw new IOException(String.format("No URL found for the %s scene!", fxmlScenePath));
        }
//        LOGGER.info("Creating a new scene for " + fxml.toExternalForm());
        final FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("lang"));
        loader.setControllerFactory(injector::getInstance);
        final Parent root = loader.load(fxml);
        final Scene scene = new Scene(root);
        final String cssRes = "/css/main.css";
        final URL css = Main.class.getResource(cssRes);
        if (css == null) {
            throw new IOException(String.format("No URL found for the %s CSS!", cssRes));
        }
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(css.toExternalForm());
//        stylesheets.add(BootstrapFX.bootstrapFXStylesheet());
        return scene;
    }
}
