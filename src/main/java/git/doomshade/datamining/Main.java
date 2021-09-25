package git.doomshade.datamining;

import git.doomshade.datamining.data.RequestHandlerRegistry;
import git.doomshade.datamining.data.handlers.DBPediaRequestHandler;
import org.apache.commons.cli.ParseException;

import java.util.logging.Logger;

import static git.doomshade.datamining.command.CommandManager.parseAndExecuteCommands;
import static git.doomshade.datamining.command.CommandManager.registerCommands;

/**
 * @author Jakub Šmrha
 * @version 1.0
 * @since 1.0
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getSimpleName());

    public static void main(String[] args) throws ParseException {
        registerCommands();
        registerRequestHandlers();
        parseAndExecuteCommands(args);
    }

    private static void registerRequestHandlers() {
        RequestHandlerRegistry.register(new DBPediaRequestHandler());
    }

    public static Logger getLogger() {
        return logger;
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
