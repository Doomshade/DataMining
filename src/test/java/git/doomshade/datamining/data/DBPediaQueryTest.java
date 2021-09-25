package git.doomshade.datamining.data;

import git.doomshade.datamining.data.dbpedia.DBPediaRequestHandler;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaQueryTest {

    public static void main(String[] args) {
        RequestHandlerRegistry.register(new DBPediaRequestHandler());
        String request = "" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX dbo: <https://dbpedia.org/ontology/>\n" +
                "SELECT *" +
                "WHERE\n" +
                "{\n" +
                "  ?athlete  rdfs:label      'Cristiano Ronaldo'@en ;\n" +
                "}";
        final DBDataResult query = RequestHandlerFactory.getDBPediaRequestHandler().query("http://dbpedia.org/resource/Charles_IV,_Holy_Roman_Emperor");
        System.out.println(query);
    }
}
