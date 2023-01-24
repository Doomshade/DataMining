package cz.zcu.jsmahy.datamining;

import com.sun.javafx.application.PlatformImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TODO
 *
 * @author Jakub Šmrha
 * @version 0.0.1
 * @since 11.04.2022
 */
public class PredecessorQueryTest {
    private static final Logger LOGGER = LogManager.getLogger(PredecessorQueryTest.class);
    private static final String RESOURCE = "<http://dbpedia.org/resource/Charles_IV,_Holy_Roman_Emperor>";

    public static void main(String[] args) {
        // because of some error (regardless of using JavaFX) we have to run this on JavaFX thread
        PlatformImpl.startup(() -> {
            // query for all predecessors (recursively)
            // "select distinct ?name ?pred\n" +
            // TODO: make a (SPARQL)QueryBuilder
            final String rawQuery = "PREFIX rdf: <https://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                    "PREFIX r: <http://dbpedia.org/resource/>\n" +
                                    "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                    "PREFIX dbp: <http://dbpedia.org/property/>\n" +
                                    "select distinct ?name\n" +
                                    "{\n" +
                                    "?pred dbp:predecessor " +
                                    RESOURCE +
                                    " .\n" +
                                    "?pred dbp:predecessor+ ?name\n" +
                                    "}\n" +
                                    "order by ?pred";
            LOGGER.debug("Raw query:\n{}", rawQuery);
        });
    }
}
