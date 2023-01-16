package cz.zcu.jsmahy.datamining.api;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public interface BlockingAmbiguousInputResolver<T, R> extends AmbiguousInputResolver<T, R, BlockingDataNodeReferenceHolder<T>> {

    boolean isFinished();

    boolean hasMultipleReferences();
}
