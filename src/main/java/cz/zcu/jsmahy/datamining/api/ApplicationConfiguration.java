package cz.zcu.jsmahy.datamining.api;

public interface ApplicationConfiguration<T, R> {
    void reload();

    /**
     * @return The progress listener for this endpoint.
     */
    RequestProgressListener<T> getProgressListener();

    /**
     * @return The data node factory.
     */
    DataNodeFactory<T> getNodeFactory();

    /**
     * @return The resolver that should be called when the program does not know which path to choose.
     */
    ResponseResolver<T, R, ?> getAmbiguousResultResolver();

    /**
     * @return The resolver that should be called when asking for the path predicate.
     */
    ResponseResolver<T, R, ?> getOntologyPathPredicateResolver();

    /**
     * @return The resolver that should be called when asking for the start and end points in time.
     */
    ResponseResolver<T, R, ?> getStartAndEndDateResolver();

    /**
     * @return The endpoint's base URL.
     */
    String getBaseUrl();
}
