package cz.zcu.jsmahy.datamining;

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
        File f = new File(FILENAME);
        InputStream in = null;
        try {
            if (!f.exists()) {
                in = Main.class.getResourceAsStream(FILENAME);
            } else {
                in = new FileInputStream(f);
            }
            DEFAULT_PROPERTIES.load(in);
        } catch (IOException e) {
            Main.getL().throwing(e);
        } finally {
            PROPERTIES = new Properties(DEFAULT_PROPERTIES);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Main.getL().throwing(e);
                }
            }
        }
    }

    public static void setupConfig(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            PROPERTIES.load(in);
        }
    }

    public static int getMaxDepth() {
        final int DEFAULT_DEPTH = 10;
        try {
            return Integer.parseInt(PROPERTIES.getProperty("max-depth", String.valueOf(DEFAULT_DEPTH)));
        } catch (NumberFormatException e) {
            Main.getL().throwing(e);
        }
        return DEFAULT_DEPTH;
    }
}
