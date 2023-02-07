package cz.zcu.jsmahy.datamining.api;

import com.google.inject.Inject;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of the application configuration.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
@Getter
public class DefaultApplicationConfiguration extends DefaultArbitraryDataHolder implements ApplicationConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(DefaultApplicationConfiguration.class);
    private static final Map<String, Field> KEY_TO_FIELD_MAP = new HashMap<>();
    private static final String CONFIG_FILE_NAME = "config.yml";

    private final Object lock = new Object();

    @Inject
    public DefaultApplicationConfiguration() {
        try {
            this.reload(new InputStreamReader(Objects.requireNonNull(this.getClass()
                                                                         .getResourceAsStream(CONFIG_FILE_NAME))));
        } catch (Exception e) {
            LOGGER.error("Failed to read {} as stream. Exception:", CONFIG_FILE_NAME, e);
        }
    }

    @Override
    public void reload(final Reader in) throws IOException, YAMLException {
        synchronized (lock) {
            final Yaml yaml = new Yaml();
            this.metadata.clear();
            try (in) {
                final Map<? extends String, ?> variables = yaml.load(in);
                this.metadata.putAll(variables);
            }
        }

    }

}
