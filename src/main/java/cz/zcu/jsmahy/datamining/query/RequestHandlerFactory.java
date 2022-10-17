package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.query.handlers.DBPediaRequestHandler;
import javafx.concurrent.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The {@link IRequestHandler} factory. Here are all the registered {@link IRequestHandler}s.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class RequestHandlerFactory {
	private static final Logger LOGGER = LogManager.getLogger(RequestHandlerFactory.class);

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
			LOGGER.info("Printing ontology: {}", ont.toString());
		});
		service.setOnFailed(x -> {
			service.getException()
			       .printStackTrace();
		});
	}
}
