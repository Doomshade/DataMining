package cz.zcu.jsmahy.datamining.api;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Getter
public class DefaultApplicationConfiguration<T, R> implements ApplicationConfiguration<T, R> {

    private static final Logger LOGGER = LogManager.getLogger(DefaultApplicationConfiguration.class);
    private static final Map<String, Field> KEY_TO_FIELD_MAP = new HashMap<>();
    private static final String CONFIG_FILE_NAME = "config.yml";

    static {
        for (Field field : DefaultApplicationConfiguration.class.getDeclaredFields()) {
            if (!field.trySetAccessible()) {
                continue;
            }
            if (field.isAnnotationPresent(ConfigurationProperty.class)) {
                final ConfigurationProperty annotation = field.getAnnotation(ConfigurationProperty.class);
                KEY_TO_FIELD_MAP.put(annotation.value(), field);
            }
        }
    }

    private final RequestProgressListener<T> progressListener;

    private final DataNodeFactory<T> dataNodeFactory;

    private final ResponseResolver<T, R, ?> ambiguousResultResolver;

    private final ResponseResolver<T, R, ?> ontologyPathPredicateResolver;

    private final ResponseResolver<T, R, ?> startAndEndDateResolver;
    private final Object lock = new Object();
    private final Map<String, Object> configVariables = new HashMap<>();

    @Inject
    @SuppressWarnings("unchecked, rawtypes")
    public DefaultApplicationConfiguration(final RequestProgressListener progressListener,
                                           final DataNodeFactory dataNodeFactory,
                                           final @Named("userAssisted") ResponseResolver ambiguousResultResolver,
                                           final @Named("ontologyPathPredicate") ResponseResolver ontologyPathPredicateResolver,
                                           final @Named("date") ResponseResolver startAndEndDateResolver) throws IOException {
        this.progressListener = progressListener;
        this.dataNodeFactory = dataNodeFactory;
        this.ambiguousResultResolver = ambiguousResultResolver;
        this.ontologyPathPredicateResolver = ontologyPathPredicateResolver;
        this.startAndEndDateResolver = startAndEndDateResolver;
        this.reload();
    }

    @Override
    public void reload() throws IOException {
        synchronized (lock) {
            final Yaml yaml = new Yaml();
            this.configVariables.clear();
            final Map<? extends String, ?> values;
            try {
                try (final InputStream in = getClass().getResourceAsStream(CONFIG_FILE_NAME)) {
                    values = yaml.load(in);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
            this.configVariables.putAll(values);
        }
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <V> V get(final String key) throws NoSuchElementException, ClassCastException {
        if (!configVariables.containsKey(key)) {
            throw new NoSuchElementException(key);
        }
        return (V) configVariables.get(key);
    }

    @Override
    public <V> List<V> getList(final String key) throws NoSuchElementException, ClassCastException {
        return get(key);
    }
}
