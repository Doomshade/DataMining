package git.doomshade.datamining.data;

import git.doomshade.datamining.data.exception.InvalidQueryException;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface IRequestHandler {

    /**
     * Queries a web page based on the handler implementation
     *
     * @param request the request to send to the web page
     *
     * @return the result of the query
     *
     * @throws InvalidQueryException if the query is invalid
     */
    DBDataResult query(String request) throws InvalidQueryException;
}
