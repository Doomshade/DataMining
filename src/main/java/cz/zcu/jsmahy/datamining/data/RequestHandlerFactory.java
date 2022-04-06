package cz.zcu.jsmahy.datamining.data;

import cz.zcu.jsmahy.datamining.data.handlers.DBPediaRequestHandler;
import javafx.concurrent.Service;

/**
 * The {@link IRequestHandler} factory. Here are all the registered {@link IRequestHandler}s.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class RequestHandlerFactory {


	/**
	 * @return the DBPedia request handler
	 */
	public static IRequestHandler getDBPediaRequestHandler() {
		return RequestHandlerRegistry.getDataRequestHandler(DBPediaRequestHandler.class);
	}

	/**
	 * Sets up the default {@link Service#onSucceededProperty()} and {@link Service#onFailedProperty()}
	 *
	 * @param service the service
	 */
	public static void setupDefaultServiceHandlers(Service<Ontology> service) {
		service.setOnSucceeded(x -> {
			final Ontology ont = (Ontology) x.getSource()
			                                 .getValue();
			ont.printOntology(System.out);
		});
		service.setOnFailed(x -> {
			service.getException()
			       .printStackTrace();
		});
	}
}
