package git.doomshade.datamining.data;

import git.doomshade.datamining.data.exception.InvalidQueryException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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
     * @param request   the request to send to the web page, e.g. {@code Windows_10}
     * @param namespace the namespace, e.g. <a href="https://dbpedia.org/property/">a property</a> or <a
     *                  href="https://dbpedia.org/ontology/">an ontology</a>
     * @param link      the link to create the ontology for, e.g. {@code precededBy}
     * @return the ontology
     * @throws InvalidQueryException if the request is invalid
     */
    Service<Ontology> query(String request, String namespace, String link) throws InvalidQueryException;

    /**
     * @return the model used in this query
     */
    Model getModel();
}
