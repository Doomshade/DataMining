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
public class Config {
    private static final String FILE_NAME = "config.properties";
    private final Logger logger = LogManager.getLogger(Config.class);
    private final Properties defaultProperties = new Properties();
    private final Properties properties = new Properties();
    //<editor-fold desc="Config properties">
    @ConfigurationProperty(value = "max-depth",
                           defaultValue = "10")
    @Getter
    private final int maxDepth = 0;

    {
        load();
    }

    Config() {
    }
    //</editor-fold>

    public void load() {
        logger.info("Loading config...");
        final File f = new File(FILE_NAME);
        InputStream in = null;
        try {
            // if the config was not found in the directory, use the
            // default one built in the jar
            if (!f.exists()) {
                in = Main.class.getClassLoader()
                               .getResourceAsStream(FILE_NAME);
            } else {
                in = new FileInputStream(f);
            }

            // load the properties
            defaultProperties.load(in);
            logger.info("Successfully loaded properties");
        } catch (IOException e) {
            logger.error("Failed to load properties", e);
        } finally {
            // load the properties and wire them to the fields
            properties.putAll(defaultProperties);
            autowire();

            // don't forget to close the file
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Failed to close the config input stream", e);
                }
            }
        }
    }

    /**
     * Maps property key/values to fields with {@link ConfigurationProperty} annotation
     */
    private void autowire() {
        logger.info("Autowiring fields...");

        // iterate through fields
        for (Field field : Config.class.getDeclaredFields()) {
            // check only the ones that are annotated with Property
            if (!field.isAnnotationPresent(ConfigurationProperty.class)) {
                continue;
            }

            final ConfigurationProperty prop = field.getAnnotation(ConfigurationProperty.class);
            // get the property value and set the field value via reflection
            try {
                final String value = properties.getProperty(prop.value(), prop.defaultValue());
                logger.trace("Setting {} to {}", field.getName(), value);
                setField(field, value);
            } catch (Exception e) {
                logger.fatal("Failed to autowire config, exiting...", e);
                Platform.exit();
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
    private void setField(final Field field, final String value) throws IllegalAccessException {
        final Class<?> type = field.getType();
        if (type == int.class) {
            field.setInt(this, Integer.parseInt(value));
        } else if (type == float.class) {
            field.setFloat(this, Float.parseFloat(value));
        } else if (type == double.class) {
            field.setDouble(this, Double.parseDouble(value));
        } else if (type == boolean.class) {
            field.setBoolean(this, Boolean.parseBoolean(value));
        } else if (type == char.class) {
            field.setChar(this, value == null || value.isEmpty() ? ' ' : value.charAt(0));
        } else {
            field.set(this, value);
        }
    }
}
