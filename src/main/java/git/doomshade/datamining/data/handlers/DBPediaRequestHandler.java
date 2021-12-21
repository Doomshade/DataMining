package git.doomshade.datamining.data.handlers;

import git.doomshade.datamining.Main;
import git.doomshade.datamining.data.*;
import git.doomshade.datamining.exception.InvalidQueryException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

/**
 * The DBPedia {@link IRequestHandler}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaRequestHandler extends AbstractRequestHandler {
    private static final String DBPEDIA_SITE = "http://dbpedia.org/resource/";
    private static boolean requesting = false;
    private final Collection<String> usedURIs = new HashSet<>();
    private Ontology currOntology = null;
    private Model model = null;
    private Request request = null;

    /**
     * Constructs a simple {@link Selector} from a subject, a predicate, and a
     * null RDFNode
     *
     * @param subject   the subject
     * @param predicate the predicate
     *
     * @return a simple selector
     */
    private static Selector getSelector(Resource subject, Property predicate) {

        return new SimpleSelector(subject, predicate, (RDFNode) null) {
            @Override
            public boolean selects(Statement s) {
                return !s.getObject().isLiteral() ||
                        s.getLanguage()
                                .equalsIgnoreCase(Locale.ENGLISH.getLanguage());
            }
        };
    }

    @Override
    protected synchronized Ontology query0(final Request request)
            throws InvalidQueryException {
        if (requesting) {
            throw new IllegalStateException("Already requesting!");
        }
        requesting = true;
        // create the root request and model
        final String r = DBPEDIA_SITE.concat(request.getRequestPage());
        final OntModel model =
                ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        try {
            model.read(r);
            //System.out.println(model);
        } catch (Exception e) {
            requesting = false;
            throw new InvalidQueryException(e);
        }

        //System.out.println(model);
        // root subject and predicate
        final Resource subject = model.getResource(r);
        final Property predicate =
                model.getProperty(request.getNamespace(), request.getLink());
        final Selector selector = getSelector(subject, predicate);

        // root link
        final Ontology ontology = new Ontology(subject);

        // prepare the fields, don't put them as parameters, it will just
        // fill stack with duplicates
        this.request = request;
        this.currOntology = ontology;
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
            // check whether the object meets requirements, i.e. check
            // restrictions
            if (!meetsRequirements(model, object)) {
                return;
            }
            // create a link and add a new edge going from previous to this
            final Ontology.Link next = createLink(prev, object);

            // the node is a resource -> means the ontology continues -> we
            // search deeper
            if (object.isURIResource()) {
                searchFurther(model, object.asResource(), next);
            }
        }
    }

    private void searchFurther(final Model model, final Resource resource,
                               final Ontology.Link next) {
        updateModel(model, resource);
        //System.out.println(model);
        if (usedURIs.add(resource.getURI())) {
            final Selector sel = getSelector(resource,
                    model.getProperty(request.getNamespace(),
                            request.getLink()));
            dfs(model, sel, next);
        }
    }

    private Ontology.Link createLink(final Ontology.Link prev,
                                     final RDFNode object) {
        Ontology.Link next = currOntology.createLink(object);
        currOntology.addEdge(prev, next);
        Main.getL().info(next.toString());
        return next;
    }

    private boolean meetsRequirements(final Model model, final RDFNode object) {
        if (!object.isURIResource()) {
            return true;
        }
        final Resource resource = object.asResource();
        updateModel(model, resource);
        for (Restriction r : request.getRestrictions()) {
            final Selector sel =
                    getSelector(resource, model.getProperty(r.getNamespace(),
                            r.getLink()));
            if (!model.listStatements(sel).hasNext()) {
                // don't continue in the search
                return false;
            }
        }
        return true;
    }

    private void updateModel(final Model model, final Resource resource) {
        String URI = resource.getURI();
        model.read(URI);
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

                final boolean isSuccessor = pred.equals(model.getProperty
                ("http://dbpedia
                .org/ontology/", "successor"))
                        || pred.equals(model.getResource("http://dbpedia
                        .org/property/successor"));
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

    /*private void printOut(Model model, Resource subject, String title,
    String type) {
        System.out.println(title);
        final Property ontPred = model.getProperty("http://dbpedia
        .org/ontology/", type);
        final Property propPred = model.getProperty("http://dbpedia
        .org/property/", type);
        System.out.println(model.listObjectsOfProperty(subject, ontPred)
        .toList());
        System.out.println(model.listObjectsOfProperty(subject, propPred)
        .toList());
        System.out.println();
    }*/


    //System.out.println(model.getProperty("https://dbpedia.org/ontology/child").getProperty(RDFS.label));
        /*try (QueryExecution query = QueryExecutionFactory.sparqlService
        (SERVICE, request)) {

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
