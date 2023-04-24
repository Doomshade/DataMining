package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.dbpedia.DBPediaEndpointTask;

/**
 * The result enum after initial search used in {@link SparqlEndpointTask}s.
 *
 * @author Jakub Šmrha
 * @see DBPediaEndpointTask
 * @since 1.0
 */
public enum InitialSearchResult {
    OK,
    SUBJECT_NOT_FOUND,
    PATH_NOT_SELECTED,
    NO_DATE_FOUND,
    START_DATE_NOT_SELECTED,
    END_DATE_NOT_SELECTED,
    UNKNOWN
}
