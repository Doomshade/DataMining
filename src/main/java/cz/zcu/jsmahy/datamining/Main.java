package cz.zcu.jsmahy.datamining;

import cz.zcu.jsmahy.datamining.command.CommandManager;
import cz.zcu.jsmahy.datamining.config.Config;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jakub Šmrha
 * @version 1.0
 * @since 1.0
 */
public class Main extends Application {
	private static final Logger L = LogManager.getLogger(Main.class);

	public static void main(String[] args) throws ParseException {
		CommandManager.parseAndExecuteCommands(args);
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		stage.setTitle("BP");
		//InfoboxManager.downloadTemplates("default-infoboxes");
		final Scene scene = SceneManager.getScene(FXMLScene.IB_TEMPLATE_MENU);
		stage.setScene(scene);
		stage.show();
		Config config = Config.getInstance();

	}

    /*OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://en.wikipedia.org/api/rest_v1/page/summary/Apple_Inc.")
                .get()
                .build();
        try {
            final Response response = client.newCall(request).execute();
            final String data = response.body().string();
            final JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
            System.out.println(jsonObject);
            final String displayTitle = jsonObject.get("displaytitle").getAsString();
            final JsonObject originalImage = jsonObject.get("originalimage").getAsJsonObject();
            final String imageURL = originalImage.get("source").getAsString();
            final String extractText = jsonObject.get("extract").getAsString();
            System.out.println(displayTitle);
            System.out.println(imageURL);
            System.out.println(extractText);

        } catch (IOException e) {
            e.printStackTrace();
        }*/
}
