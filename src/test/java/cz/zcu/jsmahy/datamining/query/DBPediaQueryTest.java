package cz.zcu.jsmahy.datamining.query;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.javafx.application.PlatformImpl;
import cz.zcu.jsmahy.datamining.guice.DBPediaModule;
import javafx.concurrent.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaQueryTest {
	public static final String SUCCESSOR = "successor";
	public static final String PREDECESSOR = "predecessor";
	private static final Logger LOGGER = LogManager.getLogger(DBPediaQueryTest.class);

	public static void main(String[] args) {
		// start the JavaFX application otherwise we get errors
		PlatformImpl.startup(() -> {
			final String requestPage = "Windows_10";
			final String namespace = "http://dbpedia.org/property/";
			final String link = "precededBy";
			LOGGER.info("Querying {}{} in namespace {} by link {}", namespace, requestPage, namespace, link);
			final Injector injector = Guice.createInjector(new DBPediaModule());
			final RequestHandler requestHandler = injector.getInstance(RequestHandler.class);
			final Service<Ontology> query = requestHandler
					.query(new SparqlRequest(
							requestPage,
							namespace,
							link
					));
			RequestHandlerFactory.setupDefaultServiceHandlers(query);
			query.start();
		});
	}
}
