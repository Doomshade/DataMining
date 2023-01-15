package cz.zcu.jsmahy.datamining.query;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public interface AsyncRequestHandler<T, R> extends RequestHandler<T, R> {

    void unlockDialogPane();
}
