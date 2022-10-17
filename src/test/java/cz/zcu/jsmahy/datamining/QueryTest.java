package cz.zcu.jsmahy.datamining;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import javafx.concurrent.Service;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @version 0.0.1
 * @since 11.04.2022
 */
public class QueryTest {
    private static final Logger LOGGER = LogManager.getLogger(QueryTest.class);
    private static final String DBPEDIA_SERVICE = "http://dbpedia.org/sparql/";

    public static void main(String[] args) {
        // because of some error (regardless of using JavaFX) we have to run this on JavaFX thread
        PlatformImpl.startup(() -> {
            // query for all predecessors (recursively) of Charles IV.
            final String q = "PREFIX rdf: <https://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX r: <http://dbpedia.org/resource/>\n" +
                    "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                    //"select distinct ?name ?pred\n" +
                    "select distinct ?name\n" +
                    "{\n" +
                    "?pred dbo:predecessor <http://dbpedia.org/resource/Charles_IV,_Holy_Roman_Emperor> .\n" +
                    "?pred dbo:predecessor+ ?name\n" +
                    "}\n" +
                    "order by ?pred";
            LOGGER.info("Query: {}", q);

            // build the query via Jena
            final Query query = QueryFactory.create(q);
            final QueryExecution qe = QueryExecution.service(DBPEDIA_SERVICE).query(query).build();
            LOGGER.info("Executing query at {}", DBPEDIA_SERVICE);

            // execute the query on a separate thread via Service
            final Service<ResultSet> queryService = new QueryService(qe);
            queryService.setOnSucceeded(e -> {
                final ResultSet results = (ResultSet) e.getSource().getValue();
                LOGGER.info("Query successfully executed");
                LOGGER.info("Query returned {} results", results.hasNext() ? "some" : "no");

                // print the results
                while (results.hasNext()) {
                    final QuerySolution soln = results.next();
                    final Resource resource = soln.getResource("name");
                    // TODO: we can now work with this resource
                    LOGGER.debug(resource);
                }
                exit();
            });

            queryService.setOnFailed(e -> {
                LOGGER.error(queryService.getException());
                exit();
            });
            queryService.setOnCancelled(e -> {
                exit();
            });
            queryService.start();
        });
    }

    private static synchronized void exit() {
        LOGGER.info("Exiting application...");
        Platform.exit();
        System.exit(0);
    }
}
