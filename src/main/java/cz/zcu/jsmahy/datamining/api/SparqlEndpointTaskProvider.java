package cz.zcu.jsmahy.datamining.api;

/**
 * A provider of {@link SparqlEndpointTask}s. Reason this exists is the {@link SparqlEndpointTask}s can have any number of arguments, and it would be hard to instantiate them via dependency injection.
 * The only way to instantiate them would be if we registered them in {@link Module}, and then called setters for the {@code query} and {@code dataNodeRoot}. This would, however, be unsafe as we could
 * forget to set those members, and thus the reason we delegate it to a provider.
 *
 * @param <R> The generic type of {@link SparqlEndpointTask}
 */
public interface SparqlEndpointTaskProvider<R> {
    /**
     * Creates a new {@link SparqlEndpointTask}
     *
     * @param query        the query
     * @param dataNodeRoot the data node root
     *
     * @return a new {@link SparqlEndpointTask}
     */
    SparqlEndpointTask<R> newTask(String query, DataNode dataNodeRoot);
}
