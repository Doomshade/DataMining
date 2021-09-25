package git.doomshade.datamining;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class Config {
    private static final Properties DEFAULT_PROPERTIES = new Properties();
    private static final Properties PROPERTIES;

    private static final String FILENAME = "config.properties";

    static {
        try {
            File f = new File(FILENAME);
            InputStream in;
            if (!f.exists()) {
                in = Main.class.getResourceAsStream(FILENAME);
            } else {
                in = new FileInputStream(f);
            }
            DEFAULT_PROPERTIES.load(in);
        } catch (IOException e) {
            Main.getLogger().throwing(Config.class.getSimpleName(), "", e);
        } finally {
            PROPERTIES = new Properties(DEFAULT_PROPERTIES);
        }
    }

    public static void setupConfig(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        PROPERTIES.load(in);
    }

    public static int getMaxDepth() {
        final int DEFAULT_DEPTH = 10;
        try {
            return Integer.parseInt(PROPERTIES.getProperty("max-depth", String.valueOf(DEFAULT_DEPTH)));
        } catch (NumberFormatException e) {
            Main.getLogger().throwing(Config.class.getSimpleName(), "getMaxDepth", e);
        }
        return DEFAULT_DEPTH;
    }
}
