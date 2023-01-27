package cz.zcu.jsmahy.datamining.config;

import com.google.inject.Inject;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Field;
import java.util.*;

@Getter
public final class DBPediaConfiguration implements DataMiningConfiguration {
    public static final Collection<String> ALL_VALID_DATE_FORMATS = new HashSet<>() {
        {
            add("integer");
            add("date");
            add("time");
            add("dateTime");
            add("dateTimeStamp");
            add("duration");
            add("duration#dayTimeDuration");
            add("duration#yearMonthDuration");
            add("gDay");
            add("gMonth");
            add("gYear");
            add("gYearMonth");
            add("gMonthDay");
        }
    };

    private static final Logger LOGGER = LogManager.getLogger(DBPediaConfiguration.class);
    private static final Map<String, Field> KEY_TO_FIELD_MAP = new HashMap<>();

    static {
        for (Field field : DBPediaConfiguration.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigurationProperty.class)) {
                final ConfigurationProperty annotation = field.getAnnotation(ConfigurationProperty.class);
                KEY_TO_FIELD_MAP.put(annotation.value(), field);
            }
        }
    }

    private final Object lock = new Object();
    private final String configFileName;

    @ConfigurationProperty("ignored-path-predicates")
    private List<String> ignoredPathPredicates;

    @ConfigurationProperty("valid-date-formats")
    private List<String> validDateFormats;

    @Inject
    public DBPediaConfiguration(final String configFileName) {
        this.configFileName = configFileName;
    }

    @Override
    public void reload() {
        synchronized (lock) {
            final Yaml yaml = new Yaml();
            final Map<String, Object> map = yaml.load(getClass().getResourceAsStream(configFileName));
            for (final Map.Entry<String, Object> entry : map.entrySet()) {
                final Field field = KEY_TO_FIELD_MAP.get(entry.getKey());
                if (field == null || !field.trySetAccessible()) {
                    continue;
                }
                final Object value = entry.getValue();
                try {
                    field.set(this, value);
                } catch (Exception e) {
                    LOGGER.error("Failed to set value {} to field '{}' (field's respective value in YAML file: '{}')",
                                 value,
                                 field.getName(),
                                 field.getAnnotation(ConfigurationProperty.class)
                                      .value());
                    LOGGER.throwing(e);
                }
            }
        }
    }
}
