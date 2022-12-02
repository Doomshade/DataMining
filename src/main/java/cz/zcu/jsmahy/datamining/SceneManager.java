package cz.zcu.jsmahy.datamining;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class SceneManager {
	public static final String FXML_PATH = "/fxml/";
	private static final Logger LOGGER = LogManager.getLogger(SceneManager.class);
	private static final String FXML_SUFFIX = ".fxml";

	static Scene getScene(final FXMLScene fxmlScene) throws IOException {
		String fxmlScenePath = fxmlScene.getScenePath();
		final int fxmlSuffixIndex = fxmlScenePath.lastIndexOf(FXML_SUFFIX);
		if (fxmlSuffixIndex >= 0) {
			LOGGER.warn("FXML Scene {} has concatenated \".fxml\". This is not needed", fxmlScene.name());
			fxmlScenePath = fxmlScenePath.substring(0, fxmlSuffixIndex);
		}

		final URL fxml = Main.class.getResource(
				FXML_PATH
						.concat(fxmlScenePath)
						.concat(FXML_SUFFIX));
		if (fxml == null) {
			throw new IOException(String.format("No URL found for the %s scene!", fxmlScenePath));
		}

		LOGGER.info("Creating a new scene for " + fxml.toExternalForm());
		final Parent root = FXMLLoader.load(fxml, ResourceBundle.getBundle("lang"));
		final Scene scene = new Scene(root);
		final String cssRes = "/css/main.css";
		final URL css = Main.class.getResource(cssRes);
		if (css == null) {
			throw new IOException(String.format("No URL found for the %s CSS!", cssRes));
		}
		scene.getStylesheets()
		     .addAll(css.toExternalForm());
		return scene;
	}
}
