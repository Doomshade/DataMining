package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.zcu.jsmahy.datamining.api.ApplicationConfiguration.*;
import static java.util.Objects.requireNonNull;

/**
 * <p>Default implementations for {@link SparqlEndpointTask}.</p>
 * <p>This class does not implement the {@link Task#call()} method, rather it returns {@link UnsupportedOperationException}! To run this task you must implement this method.</p>
 *
 * @param <R>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultSparqlEndpointTask<R> extends SparqlEndpointTask<R> {
    protected final Collection<String> ignoredPathPredicates = new HashSet<>();
    protected final Collection<String> validDateFormats = new HashSet<>();
    protected final ApplicationConfiguration config;
    protected final RequestProgressListener progressListener;

    protected final String query;
    protected final String originalQuery;
    protected final DataNode dataNodeRoot;

    public DefaultSparqlEndpointTask(final String query, final DataNode dataNodeRoot, final ApplicationConfiguration config, final RequestProgressListener progressListener) {
        this.config = requireNonNull(config);
        this.dataNodeRoot = requireNonNull(dataNodeRoot);
        this.progressListener = requireNonNull(progressListener);

        this.originalQuery = requireNonNull(query);
//        final String baseUrl = "http://cs.dbpedia.org/resource/";
        this.query = transformQuery(originalQuery, config.getValueUnsafe(CFG_KEY_BASE_URL));

        final List<String> ignoredPathPredicates = config.getValueUnsafe(CFG_KEY_IGNORE_PATH_PREDICATES);
        this.ignoredPathPredicates.addAll(ignoredPathPredicates);

        final List<String> validDateFormats = config.getValueUnsafe(CFG_KEY_VALID_DATE_FORMATS);
        this.validDateFormats.addAll(transformValidDateFormats(validDateFormats));
    }

    public static Collection<String> transformValidDateFormats(Collection<String> validDateFormats) {
        final Set<String> validDateFormatsSet = validDateFormats.stream()
                                                                .map(String::toLowerCase)
                                                                .collect(Collectors.toSet());
        if (validDateFormatsSet.contains(CFG_DATE_FORMAT_ANY)) {
            return ALL_VALID_DATE_FORMATS;
        } else {
            return validDateFormatsSet;
        }
    }

    public static String transformQuery(final String query, final String baseUrl) {
        final boolean hasBaseUrl = query.startsWith(baseUrl);
        if (!hasBaseUrl) {
            return baseUrl.concat(query);
        } else {
            return query;
        }
    }

    public synchronized void unlockDialogPane() {
        notify();
    }

    @Override
    public R call() throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }
}
