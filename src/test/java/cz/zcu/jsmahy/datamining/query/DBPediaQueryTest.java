package cz.zcu.jsmahy.datamining.query;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.javafx.application.PlatformImpl;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import javafx.concurrent.Service;
import javafx.scene.control.TreeItem;
import org.apache.jena.rdf.model.RDFNode;
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
            LOGGER.info("Querying {}{} in namespace {} by link {}", "http://dbpedia.org/resource/", requestPage, namespace, link);
            final Injector injector = Guice.createInjector(new DBPediaModule());
            final RequestHandler<RDFNode, Void> requestHandler = injector.getInstance(RequestHandler.class);
            final SparqlRequest<RDFNode, Void> request = new SparqlRequest<>(requestPage, namespace, link, new TreeItem<>(), new UserAssistedAmbiguitySolver<>());
            final Service<Void> query = requestHandler.query(request);
            query.setOnSucceeded(x -> {
            });
            query.setOnFailed(x -> {
                query.getException()
                     .printStackTrace();
            });
            query.start();
        });
    }
}
