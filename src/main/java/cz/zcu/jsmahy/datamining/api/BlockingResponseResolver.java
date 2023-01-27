package cz.zcu.jsmahy.datamining.api;

/**
 * Resolver that <b>does</b> block the thread.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public interface BlockingResponseResolver<T, R> extends ResponseResolver<T, R, BlockingDataNodeReferenceHolder<T>> {
}
