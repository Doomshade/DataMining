package git.doomshade.datamining.exception;

/**
 * An invalid query exception
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class InvalidQueryException extends RuntimeException {

    public InvalidQueryException() {
        super();
    }

    public InvalidQueryException(String message) {
        super(message);
    }

    public InvalidQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidQueryException(Throwable cause) {
        super(cause);
    }

    public InvalidQueryException(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
