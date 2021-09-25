package git.doomshade.datamining.data;

import git.doomshade.datamining.data.handlers.DBPediaRequestHandler;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaQueryTest {

    public static final String SUCCESSOR = "successor";
    public static final String PREDECESSOR = "predecessor";

    public static void main(String[] args) {
        RequestHandlerRegistry.register(new DBPediaRequestHandler());
        final String CHARLES = "Charles_IV,_Holy_Roman_Emperor";
        final String LOUIS_XVI = "Louis_XVI";
        final String PHILIP_II = "Philip_II_of_France";
        Ontology query = RequestHandlerFactory.getDBPediaRequestHandler().query(PHILIP_II, "http://dbpedia" +
                ".org/ontology/", SUCCESSOR);
        query.printOntology(System.out);
        query = RequestHandlerFactory.getDBPediaRequestHandler().query("Windows_10", "http://dbpedia.org/property/",
                "precededBy");
        query.printOntology(System.out);
        //System.out.println(query);
    }
}
