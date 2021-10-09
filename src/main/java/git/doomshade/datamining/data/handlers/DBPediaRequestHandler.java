package git.doomshade.datamining.data.handlers;

import git.doomshade.datamining.Main;
import git.doomshade.datamining.data.AbstractRequestHandler;
import git.doomshade.datamining.data.IRequestHandler;
import git.doomshade.datamining.data.Ontology;
import git.doomshade.datamining.data.exception.InvalidQueryException;
import javafx.concurrent.Task;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

/**
 * The DBPedia {@link IRequestHandler}
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaRequestHandler extends AbstractRequestHandler {
    private static final String SERVICE = "https://dbpedia.org/sparql";
    private static final String DBPEDIA_SITE = "http://dbpedia.org/resource/";
    private final Collection<String> usedURIs = new HashSet<>();
    private String currLink = "";
    private Ontology currOntology = null;
    private String currNamespace = "";
    private Model model = null;
    private static boolean requesting = false;

    /**
     * Constructs a simple {@link Selector} from a subject, a predicate, and a null RDFNode
     *
     * @param subject   the subject
     * @param predicate the predicate
     * @return a simple selector
     */
    private static Selector getSelector(Resource subject, Property predicate) {

        return new SimpleSelector(subject, predicate, (RDFNode) null) {
            @Override
            public boolean selects(Statement s) {
                return !s.getObject().isLiteral() || s.getLanguage().equalsIgnoreCase(Locale.ENGLISH.getLanguage());
            }
        };
    }

    @Override
    protected synchronized Ontology query0(final String r, final String namespace, final String link)
            throws InvalidQueryException {
        if (requesting){
            throw new IllegalStateException("Already requesting!");
        }
        requesting = true;
        // create the root request and model
        final String request = DBPEDIA_SITE.concat(r);
        final OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        try {
            model.read(request);
        } catch (Exception e) {
            requesting = false;
            throw new InvalidQueryException(e);
        }

        //System.out.println(model);
        // root subject and predicate
        final Resource subject = model.getResource(request);
        final Property predicate = model.getProperty(namespace, link);
        final Selector selector = getSelector(subject, predicate);

        // root link
        Ontology ontology = new Ontology(subject);

        // prepare the fields, don't put them as parameters, it will just fill stack with duplicates
        this.currLink = link;
        this.currOntology = ontology;
        this.currNamespace = namespace;
        this.model = model;
        this.usedURIs.clear();

        // now iterate recursively
        dfs(model, selector, ontology.getStart());
        requesting = false;
        return ontology;
    }

    /**
     * Performs a DFS on the given model, given selector, and a previous link
     *
     * @param model    the model
     * @param selector the selector
     * @param prev     the previous link
     */
    private void dfs(Model model, Selector selector, Ontology.Link prev) {
        // list all statements based on the selector
        // only one statement should be found based on that selector
        for (final Statement stmt : model.listStatements(selector).toList()) {
            final RDFNode object = stmt.getObject();

            // create a link and add a new edge going from previous to this
            Ontology.Link next = currOntology.createLink(object);
            currOntology.addEdge(prev, next);
            Main.getLogger().info(next.toString());

            // the node is a resource -> means the ontology continues -> we search deeper
            if (object.isURIResource()) {
                final String URI = object.asResource().getURI();
                model.read(URI);
                //System.out.println(model);
                if (usedURIs.add(URI)) {
                    final Selector sel = getSelector(object.asResource(), model.getProperty(currNamespace, currLink));
                    dfs(model, sel, next);
                }
            }
        }
    }

    @Override
    public Model getModel() {
        return model;
    }

    /*private DBQueryResult getDbQueryResult(String request, Model model) {
        System.out.println("STMTS:");
        Selector sel = new SimpleSelector() {
            @Override
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
        for (Statement n : model.listStatements(sel).toList()) {
            successors.add(n.getObject().toString());
            System.out.println("XXXX");
            System.out.println("Subj: " + n.getSubject());

            System.out.println("Pred: " + n.getPredicate());

            System.out.println("Obj: " + n.getObject());
            System.out.println("XXXX");
            System.out.println();
        }
        String s = String.join(", ", successors);
        return new DBQueryResult(s, RDFFormat.JSON);
    }*/

    /*private void printOut(Model model, Resource subject, String title, String type) {
        System.out.println(title);
        final Property ontPred = model.getProperty("http://dbpedia.org/ontology/", type);
        final Property propPred = model.getProperty("http://dbpedia.org/property/", type);
        System.out.println(model.listObjectsOfProperty(subject, ontPred).toList());
        System.out.println(model.listObjectsOfProperty(subject, propPred).toList());
        System.out.println();
    }*/


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
