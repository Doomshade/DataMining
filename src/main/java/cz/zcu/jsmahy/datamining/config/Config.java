package cz.zcu.jsmahy.datamining.config;

import cz.zcu.jsmahy.datamining.Main;
import javafx.application.Platform;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Configuration file utility
 *
 * @author Jakub Šmrha
 * @version 1.0
 * @since 1.0
 */
public final class Config {
    //<editor-fold desc="Static fields">
    private static final Logger L = LogManager.getLogger(Config.class);
    private static final Properties DEFAULT_PROPERTIES = new Properties();
    private static final Properties PROPERTIES = new Properties();
    private static final String FILENAME = "config.properties";
    private static final Config INSTANCE = new Config();

    static {
        load();
    }
    //</editor-fold>

    //<editor-fold desc="Config properties">
    @Property(key = "max-depth",
              defaultValue = "10")
    @Getter
    private int maxDepth;
    //</editor-fold>

    public static void load() {
        L.info("Loading config...");
        final File f = new File(FILENAME);
        InputStream in = null;
        try {
            // if the config was not found in the directory, use the
            // default one built in the jar
            if (!f.exists()) {
                in = Main.class.getClassLoader()
                               .getResourceAsStream(FILENAME);
            } else {
                in = new FileInputStream(f);
            }

            // load the properties
            DEFAULT_PROPERTIES.load(in);
            L.info("Successfully loaded properties");
        } catch (IOException e) {
            L.warn("Failed to load properties. Reason:");
            L.catching(e);
        } finally {
            // load the properties and wire them to the fields
            PROPERTIES.putAll(DEFAULT_PROPERTIES);
            autowire();

            // don't forget to close the file
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    L.warn("Failed to close the config input stream! Reason:");
                    L.catching(e);
                }
            }
        }
    }

    /**
     * Maps property key/values to fields with {@link Property} annotation
     */
    private static void autowire() {
        L.info("Autowiring fields...");

        // iterate through fields
        for (Field field : Config.class.getDeclaredFields()) {
            // check only the ones that are annotated with Property
            if (!field.isAnnotationPresent(Property.class)) {
                continue;
            }

            final Property prop = field.getAnnotation(Property.class);
            // get the property value and set the field value via reflection
            try {
                final String value = PROPERTIES.getProperty(prop.key(), prop.defaultValue());
                L.trace("Setting {} to {}", field.getName(), value);
                setField(field, value);
            } catch (Exception e) {
                L.fatal("Failed to autowire config, exiting...");
                L.catching(e);
                Platform.exit();
                System.exit(1);
            }
        }
    }

    /**
     * Sets the field to the appropriate value
     *
     * @param field the field
     * @param value the value
     *
     * @throws IllegalAccessException if the field could not be set
     */
    private static void setField(final Field field, final String value) throws IllegalAccessException {
        final Class<?> type = field.getType();
        if (type == int.class) {
            field.setInt(INSTANCE, Integer.parseInt(value));
        } else if (type == float.class) {
            field.setFloat(INSTANCE, Float.parseFloat(value));
        } else if (type == double.class) {
            field.setDouble(INSTANCE, Double.parseDouble(value));
        } else if (type == boolean.class) {
            field.setBoolean(INSTANCE, Boolean.parseBoolean(value));
        } else if (type == char.class) {
            field.setChar(INSTANCE, value == null || value.isEmpty() ? ' ' : value.charAt(0));
        } else {
            field.set(INSTANCE, value);
        }
    }

    public static Config getInstance() {
        return INSTANCE;
    }
}
