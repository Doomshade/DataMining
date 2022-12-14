package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

/**
 * A handler for query requests
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface RequestHandler<T> {

	/**
	 * @return the model used in this query
	 */
	Model getModel();

	/**
	 * Queries a web page based on the handler implementation
	 *
	 * @param request the request to send to the web page
	 *
	 * @return the ontology
	 *
	 * @throws InvalidQueryException if the request is invalid
	 */
	Service<Ontology> query(SparqlRequest<T> request) throws InvalidQueryException;
}
