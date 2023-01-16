package cz.zcu.jsmahy.datamining.api;

/**
 * Resolver that <b>does</b> block the thread.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public interface BlockingAmbiguousInputResolver<T, R> extends AmbiguousInputResolver<T, R, BlockingDataNodeReferenceHolder<T>> {
}
