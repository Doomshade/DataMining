package cz.zcu.jsmahy.datamining.config;

import com.google.inject.Inject;
import cz.zcu.jsmahy.datamining.Main;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public final class DBPediaConfiguration implements DataMiningConfiguration {
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

    @ConfigurationProperty("ignored-predicates")
    private List<String> ignoredPredicates;

    @Inject
    public DBPediaConfiguration(final String configFileName) {
        this.configFileName = configFileName;
    }

    @Override
    public void reload() throws ReflectiveOperationException {
        synchronized (lock) {
            final Yaml yaml = new Yaml();
            final Map<String, Object> map = yaml.load(Main.class.getResourceAsStream(configFileName));
            for (final Map.Entry<String, Object> entry : map.entrySet()) {
                final Field field = KEY_TO_FIELD_MAP.get(entry.getKey());
                if (field == null || !field.trySetAccessible()) {
                    continue;
                }
                field.set(this, entry.getValue());
            }
        }
    }
}
