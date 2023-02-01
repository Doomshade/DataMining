package cz.zcu.jsmahy.datamining.api;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.*;

@Getter
public class DefaultApplicationConfiguration<R> implements ApplicationConfiguration<R> {

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

    private final RequestProgressListener progressListener;

    private final DataNodeFactory dataNodeFactory;

    private final ResponseResolver<R, ?> ambiguousResultResolver;

    private final ResponseResolver<R, ?> ontologyPathPredicateResolver;

    private final ResponseResolver<R, ?> startAndEndDateResolver;
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
        this.reload(new InputStreamReader(Objects.requireNonNull(this.getClass()
                                                                     .getResourceAsStream(CONFIG_FILE_NAME))));
    }

    @Override
    public void reload(final Reader in) throws IOException {
        synchronized (lock) {
            final Yaml yaml = new Yaml();
            this.configVariables.clear();
            try {
                try (in) {
                    final Map<? extends String, ?> variables = yaml.load(in);
                    this.configVariables.putAll(variables);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <V> Optional<V> get(final String key) throws NoSuchElementException, ClassCastException {
        return (Optional<V>) Optional.ofNullable(configVariables.get(key));
    }

    @Override
    public <V> Optional<List<V>> getList(final String key) throws NoSuchElementException, ClassCastException {
        return get(key);
    }

    @Override
    public boolean has(final String key) {
        return configVariables.containsKey(key);
    }

    @Override
    public <V> @NonNull V getUnsafe(final String key) throws NoSuchElementException, ClassCastException {
        final Optional<V> opt = get(key);
        return opt.orElseThrow(() -> new NoSuchElementException(key));
    }

    @Override
    public @NonNull <V> List<V> getListUnsafe(final String key) throws NoSuchElementException, ClassCastException {
        return getUnsafe(key);
    }
}
