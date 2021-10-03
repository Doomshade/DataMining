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
        if (args.length < 3) {
            return;
        }
        Ontology query = RequestHandlerFactory.getDBPediaRequestHandler().query(args[0], args[1], args[2]);
        query.printOntology(System.out);
        /*query = RequestHandlerFactory.getDBPediaRequestHandler().query("Windows_10", "http://dbpedia.org/property/",
                "precededBy");
        query.printOntology(System.out);*/
        //System.out.println(query);
    }
}
