package cz.zcu.jsmahy.datamining.api;

/**
 * Resolver that <b>does not</b> block the thread.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public interface DefaultAmbiguousInputResolver<T, R> extends AmbiguousInputResolver<T, R, DataNodeReferenceHolder<T>> {
}
