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
    protected final ApplicationConfiguration<R> config;
    protected final String query;
    protected final DataNodeRoot dataNodeRoot;

    public DefaultSparqlEndpointTask(final ApplicationConfiguration<R> config, String query, final DataNodeRoot dataNodeRoot) {
        this.config = requireNonNull(config);
        this.dataNodeRoot = requireNonNull(dataNodeRoot);

        query = requireNonNull(query);
        final String baseUrl = config.getUnsafe(CFG_KEY_BASE_URL);
        final boolean hasBaseUrl = query.startsWith(baseUrl);
        if (hasBaseUrl) {
            this.query = query;
        } else {
            this.query = baseUrl.concat(query);
        }

        final List<String> ignoredPathPredicates = config.getListUnsafe(CFG_KEY_IGNORE_PATH_PREDICATES);
        this.ignoredPathPredicates.addAll(ignoredPathPredicates);
        final List<String> validDateFormats = config.getListUnsafe(CFG_KEY_VALID_DATE_FORMATS);

        final Set<String> validDateFormatsSet = validDateFormats.stream()
                                                                .map(String::toLowerCase)
                                                                .collect(Collectors.toSet());
        if (validDateFormatsSet.contains(CFG_DATE_FORMAT_ANY)) {
            this.validDateFormats.addAll(ApplicationConfiguration.CollectionConstants.getAllValidDateFormats());
        } else {
            this.validDateFormats.addAll(validDateFormatsSet);
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
