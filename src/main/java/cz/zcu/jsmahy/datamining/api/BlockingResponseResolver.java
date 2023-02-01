package cz.zcu.jsmahy.datamining.api;

/**
 * Resolver that <b>does</b> block the thread.
 *
 * @param <R>     The generic type of {@link SparqlEndpointTask}
 * @param <DNRef> The {@link DataNodeReferenceHolder} (this allows us to return any implementation, see {@link BlockingResponseResolver})
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public interface BlockingResponseResolver<R, DNRef extends BlockingDataNodeReferenceHolder> extends ResponseResolver<R, DNRef> {}
