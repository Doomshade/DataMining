package cz.zcu.jsmahy.datamining;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.ParseException;

/**
 * The entry point of the application.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class Main extends Application {
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
        // use resource bundle
        stage.setTitle("BP");
        final Scene scene = SceneManager.getScene(FXMLScene.MAIN);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
