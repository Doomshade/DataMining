package cz.zcu.jsmahy.datamining.api;

/**
 * Resolver that <b>does</b> block the thread.
 *
 * @param <T>     The data type of {@link DataNode}
 * @param <R>     The generic type of {@link SparqlEndpointTask}
 * @param <DNRef> The {@link DataNodeReferenceHolder} (this allows us to return any implementation, see {@link BlockingResponseResolver})
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public interface BlockingResponseResolver<T, R, DNRef extends BlockingDataNodeReferenceHolder<T>> extends ResponseResolver<T, R, DNRef> {}
