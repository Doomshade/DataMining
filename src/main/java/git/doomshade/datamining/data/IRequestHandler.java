package git.doomshade.datamining.data;

import git.doomshade.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;
import org.apache.jena.rdf.model.Model;

/**
 * A handler for query requests
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface IRequestHandler {

    /**
     * Queries a web page based on the handler implementation
     *
     * @param request the request to send to the web page
     *
     * @return the ontology
     *
     * @throws InvalidQueryException if the request is invalid
     */
    Service<Ontology> query(Request request) throws InvalidQueryException;

    /**
     * @return the model used in this query
     */
    Model getModel();
}
