package git.doomshade.datamining;

import git.doomshade.datamining.data.RequestHandlerRegistry;
import git.doomshade.datamining.data.handlers.DBPediaRequestHandler;
import git.doomshade.datamining.io.InfoboxManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.apache.logging.log4j.spi.LoggerContext;

import static git.doomshade.datamining.command.CommandManager.parseAndExecuteCommands;

/**
 * @author Jakub Šmrha
 * @version 1.0
 * @since 1.0
 */
public class Main extends Application {
    private static final Logger L = LogManager.getLogger();

    public static void main(String[] args) throws ParseException {
        registerRequestHandlers();
        parseAndExecuteCommands(args);
        launch(args);
    }

    private static void registerRequestHandlers() {
        RequestHandlerRegistry.register(new DBPediaRequestHandler());
    }

    public static Logger getL() {
        return L;
    }

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setTitle("BP");
        //InfoboxManager.downloadTemplates("default-infoboxes");
        final Scene scene = SceneManager.getScene(FXMLScene.IB_TEMPLATE_MENU);
        stage.setScene(scene);
        stage.show();

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
