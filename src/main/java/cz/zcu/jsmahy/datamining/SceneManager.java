package cz.zcu.jsmahy.datamining;

import cz.zcu.jsmahy.datamining.util.Utils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class SceneManager {

    public static Scene getScene(final FXMLScene fxmlScene) throws IOException {
        final URL fxml = Main.class.getResource(
                Utils.FXML_PATH
                        .concat(fxmlScene.getScene())
                        .concat(Utils.FXML_EXTENSION));
        if (fxml == null) {
            throw new IOException(String.format("No URL found for the %s scene!", fxmlScene.getScene()));
        }
        Parent root = FXMLLoader.load(
                fxml,
                ResourceBundle.getBundle("lang"));
        final Scene scene = new Scene(root);
        final String cssRes = "/css/main.css";
        final URL css = Main.class.getResource(cssRes);
        if (css == null) {
            throw new IOException(String.format("No URL found for the %s CSS!", cssRes));
        }
        scene.getStylesheets().addAll(css.toExternalForm());
        return scene;
    }
}
