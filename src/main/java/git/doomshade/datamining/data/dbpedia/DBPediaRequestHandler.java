package git.doomshade.datamining.data.dbpedia;

import git.doomshade.datamining.data.DBDataResult;
import git.doomshade.datamining.data.IRequestHandler;
import git.doomshade.datamining.data.RDFFormat;
import git.doomshade.datamining.data.exception.InvalidQueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The DBPedia {@link IRequestHandler}
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaRequestHandler implements IRequestHandler {
    private static final String SERVICE = "https://dbpedia.org/sparql";

    @Override
    public DBDataResult query(String request) throws InvalidQueryException {
        Model model = ModelFactory.createDefaultModel();
        //final OntModel testm = ModelFactory.createOntologyModel(ProfileRegistry.RDFS_LANG);
        //System.out.println(testm);
        try {
            model.read(request);
            model.write(System.out);
        } catch (Exception e) {
            throw new InvalidQueryException(e);
        }
        /*System.out.println("OBJS:");
        for (RDFNode n : model.listObjects().toList()){
            System.out.println(n);
        }
        System.out.println("SUBJS:");
        for (RDFNode n : model.listSubjects().toList()){
            System.out.println(n);
        }*/
        System.out.println("STMTS:");

        System.out.println(
                model.listObjectsOfProperty(model.getProperty("http://dbpedia.org/ontology/", "predecessor")).toList());
        Selector sel = new SimpleSelector() {
            @Override
            // https://dbpedia.org/property/successor
            public boolean test(Statement s) {
                final Property pred = s.getPredicate();
                final Resource subj = s.getSubject();
                final boolean isCharles = subj.getURI().equals(request);

                final boolean isSuccessor = pred.equals(model.getProperty("http://dbpedia.org/ontology/", "successor"))
                        || pred.equals(model.getResource("http://dbpedia.org/property/successor"));
                return isCharles && isSuccessor;
            }
        };
        Set<String> successors = new HashSet<>();
        for (Statement n :
                model.listStatements(sel).toList()) {
            successors.add(n.getObject().toString());
            System.out.println("XXXX");
            System.out.println("Subj: " + n.getSubject());

            final Property pred = n.getPredicate();
            System.out.println("Pred: " + pred.toString());
            System.out.println("Pred ns: " + pred.getNameSpace());
            System.out.println("Pred ln: " + pred.getLocalName());

            final Resource res = n.getObject().asResource();
            System.out.println("Obj: " + res.toString());
            System.out.println("Obj ns: " + res.getNameSpace());
            System.out.println("Obj ln: " + res.getLocalName());
            System.out.println("XXXX");
            System.out.println();
        }
        String s = String.join(", ", successors);
        return new DBDataResult(s, RDFFormat.JSON);
        //System.out.println(model.getProperty("https://dbpedia.org/ontology/child").getProperty(RDFS.label));
        /*try (QueryExecution query = QueryExecutionFactory.sparqlService(SERVICE, request)) {

            final ResultSet set = query.execSelect();

            StringBuilder sb = new StringBuilder();
            while (set.hasNext()) {
                final QuerySolution qs = set.next();
                final RDFNode node = qs.get("athlete");
                sb.append(node.toString());
                sb.append("\n");
                //final Resource resource = node.asResource();

                // Model m = ModelFactory.createDefaultModel();
                // try {
                //     m.read(resource.getURI());
                //     m.write(System.out);
                // } catch (Exception e) {
                //     throw new InvalidQueryException(e);
                // }

            }
            return new DBDataResult(sb.toString(), RDFFormat.JSON);
        } catch (NullPointerException ex) {
            throw new InvalidQueryException(ex);
        }*/
    }
}
