package cz.zcu.jsmahy.datamining.api;

import lombok.NonNull;

import java.io.IOException;
import java.util.*;

/**
 * Responsible for loading configuration and other
 *
 * @param <R> The generic type of {@link SparqlEndpointTask}
 */
public interface ApplicationConfiguration<R> {
    String IGNORE_PATH_PREDICATES = "ignored-path-predicates";
    String VALID_DATE_FORMATS = "valid-date-formats";
    String BASE_URL = "base-url";

    /**
     * Reads the configuration file and reloads the mapped values.
     */
    void reload() throws IOException;

    /**
     * @param key the key the variable is stored under
     * @param <V> the variable type
     *
     * @return The variable inside the config file.
     *
     * @throws ClassCastException if the variable could not be cast
     */
    @NonNull <V> Optional<V> get(String key) throws ClassCastException;

    /**
     * @param key the key the variable is stored under
     * @param <V> the variable type
     *
     * @return A {@link List} of variables. Calls {@link ApplicationConfiguration#get(String)} and types it to {@code List<V>}.
     *
     * @throws ClassCastException if the variable could not be cast
     * @see ApplicationConfiguration#get(String)
     */
    <V> Optional<List<V>> getList(String key) throws ClassCastException;

    /**
     * @param key the key
     *
     * @return {@code true} if a value is stored under the key
     */
    boolean has(String key);

    /**
     * @param key the key the variable is stored under
     * @param <V> the variable type
     *
     * @return The variable inside the config file.
     *
     * @throws NoSuchElementException if no such key is mapped to a variable
     * @throws ClassCastException     if the variable could not be cast
     */
    @NonNull <V> V getUnsafe(String key) throws NoSuchElementException, ClassCastException;

    /**
     * @param key the key the variable is stored under
     * @param <V> the variable type
     *
     * @return A {@link List} of variables. Calls {@link ApplicationConfiguration#getUnsafe(String)} and types it to {@code List<V>}.
     *
     * @throws NoSuchElementException if no such key is mapped to a variable
     * @throws ClassCastException     if the variable could not be cast
     * @see ApplicationConfiguration#getUnsafe(String)
     */
    @NonNull <V> List<V> getListUnsafe(String key) throws NoSuchElementException, ClassCastException;

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
