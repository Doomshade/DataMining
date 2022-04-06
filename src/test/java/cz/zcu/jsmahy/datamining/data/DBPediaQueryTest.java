package cz.zcu.jsmahy.datamining.data;

import com.sun.javafx.application.PlatformImpl;
import javafx.concurrent.Service;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaQueryTest {

    public static final String SUCCESSOR = "successor";
    public static final String PREDECESSOR = "predecessor";

    public static void main(String[] args) {
        // start the JavaFX application otehrwise we get errors
        PlatformImpl.startup(() -> {
        });

		/*
		if (args.length < 3) {
			return;
		}
		Service<Ontology> query = RequestHandlerFactory.getDBPediaRequestHandler()
		                                               .query(new Request(args[0], args[1], args[2]));
		RequestHandlerFactory.setupDefaultServiceHandlers(query);
		query.restart();
		 */
        final String requestPage = "Windows_10";
        final String namespace = "http://dbpedia.org/property/";
        final String link = "precededBy";
        System.out.printf("Querying %s/%s in namespace %s by link %s...%n", "http://dbpedia.org/resource", requestPage, namespace, link);
        final Service<Ontology> query = RequestHandlerFactory.getDBPediaRequestHandler()
                .query(new SparqlRequest(
                        requestPage,
                        namespace,
                        link
                ));
        RequestHandlerFactory.setupDefaultServiceHandlers(query);
        query.start();
        //System.out.println(query);
    }
}
