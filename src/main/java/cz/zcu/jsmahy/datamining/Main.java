package cz.zcu.jsmahy.datamining;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.ParseException;

import java.util.ResourceBundle;

/**
 * The entry point of the application.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class Main extends Application {
    // no need to make this a singleton
    private static final SceneManager SCENE_MANAGER = new SceneManager();
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
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
        stage.setTitle(resourceBundle.getString("stage-title"));
        final Scene scene = SCENE_MANAGER.getScene(FXMLScene.MAIN);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
