package cz.zcu.jsmahy.datamining.api;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Default implementation of the application configuration.
 *
 * @param <R> {@inheritDoc}
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
@Getter
public class DefaultApplicationConfiguration<R> extends DefaultArbitraryDataHolder implements ApplicationConfiguration<R> {

    private static final Logger LOGGER = LogManager.getLogger(DefaultApplicationConfiguration.class);
    private static final Map<String, Field> KEY_TO_FIELD_MAP = new HashMap<>();
    private static final String CONFIG_FILE_NAME = "config.yml";

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
                                           final @Named("date") ResponseResolver startAndEndDateResolver) {
        this.progressListener = progressListener;
        this.dataNodeFactory = dataNodeFactory;
        this.ambiguousResultResolver = ambiguousResultResolver;
        this.ontologyPathPredicateResolver = ontologyPathPredicateResolver;
        this.startAndEndDateResolver = startAndEndDateResolver;
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
