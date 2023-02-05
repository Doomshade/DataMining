package cz.zcu.jsmahy.datamining.api;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * <p>Responsible for loading configuration and other</p>
 *
 * @param <R> The generic type of {@link SparqlEndpointTask}
 */
public interface ApplicationConfiguration<R> extends ArbitraryDataHolder {
    // general config keys
    // CFG prefix works as "configuration" prefix
    String CFG_KEY_IGNORE_PATH_PREDICATES = "ignored-path-predicates";
    String CFG_KEY_VALID_DATE_FORMATS = "valid-date-formats";
    String CFG_KEY_BASE_URL = "base-url";
    String CFG_DATE_FORMAT_ANY = "any";

    /**
     * Reads the input stream and reloads the mapped values.
     *
     * @param inputStream The input stream to load the configuration from
     */
    void reload(final Reader inputStream) throws IOException;

    /**
     * @return The progress listener for this endpoint.
     */
    RequestProgressListener getProgressListener();

    /**
     * @return The data node factory.
     */
    DataNodeFactory getDataNodeFactory();

    /**
     * @return The resolver that should be called when the program does not know which path to choose.
     */
    ResponseResolver<R, ?> getAmbiguousResultResolver();

    /**
     * @return The resolver that should be called when asking for the path predicate.
     */
    ResponseResolver<R, ?> getOntologyPathPredicateResolver();

    /**
     * @return The resolver that should be called when asking for the start and end points in time.
     */
    ResponseResolver<R, ?> getStartAndEndDateResolver();

    /**
     * List of {@link Collection} constants (just to make them unmodifiable).
     */
    class CollectionConstants {
        private static final Collection<String> ALL_VALID_DATE_FORMATS = new HashSet<>() {
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

        public static Collection<String> getAllValidDateFormats() {
            return Collections.unmodifiableCollection(ALL_VALID_DATE_FORMATS);
        }
    }
}
