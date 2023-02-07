package cz.zcu.jsmahy.datamining.api;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * <p>Responsible for loading data from a configuration file via {@link ApplicationConfiguration#reload(Reader)}.</p>
 */
public interface ApplicationConfiguration extends ArbitraryDataHolder {
    // general config keys
    // CFG prefix works as "configuration" prefix
    String CFG_KEY_IGNORE_PATH_PREDICATES = "ignored-path-predicates";
    String CFG_KEY_VALID_DATE_FORMATS = "valid-date-formats";
    String CFG_KEY_BASE_URL = "base-url";
    String CFG_DATE_FORMAT_ANY = "any";
    Collection<String> ALL_VALID_DATE_FORMATS = Collections.unmodifiableCollection(new HashSet<>() {
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
    });

    /**
     * Reads the input stream and reloads the mapped values.
     *
     * @param inputStream The input stream to load the configuration from
     */
    void reload(final Reader inputStream) throws IOException;
}
