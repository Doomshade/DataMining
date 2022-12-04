package cz.zcu.jsmahy.datamining.query.handlers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import cz.zcu.jsmahy.datamining.query.*;
import javafx.application.Platform;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * The DBPedia {@link RequestHandler}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaRequestHandler extends AbstractRequestHandler {
    private static final Logger L = LogManager.getLogger(DBPediaRequestHandler.class);
    private static final String DBPEDIA_SITE = "http://dbpedia.org/resource/";
    private static boolean requesting = false;
    private final Collection<String> usedURIs = new HashSet<>();
    private Ontology currOntology = null;
    private Model model = null;
    private SparqlRequest request = null;


    /**
     * Constructs a simple {@link Selector} from a subject, a predicate, and a null RDFNode
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
                return !s.getObject()
                         .isLiteral() || s.getLanguage()
                                          .equalsIgnoreCase(Locale.ENGLISH.getLanguage());
            }
        };
    }

    @Override
    protected synchronized Ontology query0(final SparqlRequest request) throws InvalidQueryException {
        if (requesting) {
            throw new IllegalStateException("Already requesting!");
        }
        requesting = true;
        // create the root request and model
        final String r = DBPEDIA_SITE.concat(request.getRequestPage());
        final OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        try {
            L.debug(String.format("Requesting %s", r));
            model.read(r);
        } catch (HttpException e) {
            requesting = false;
            throw L.throwing(e);
        } catch (Exception e) {
            requesting = false;
            throw new InvalidQueryException(e);
        }

        // root subject and predicate
        final Resource subject = model.getResource(r);
        final Property predicate = model.getProperty(request.getNamespace(), request.getLink());
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
        L.debug("Searching...");
        final Injector injector = Guice.createInjector(new DBPediaModule());
        injector.injectMembers(this);
        final DataNodeFactory<RDFNode> nodeFactory = injector.getInstance(DataNodeFactory.class);
        final DataNodeRoot<RDFNode> root = nodeFactory.newRoot();
        final AmbiguitySolver<RDFNode> ambiguitySolver = new DefaultAllAmbiguitySolver();
        bfs(model, selector, nodeFactory, root, ambiguitySolver);
        L.debug("Done searching");
        requesting = false;
        return ontology;
    }


    private static class DefaultAllAmbiguitySolver implements AmbiguitySolver<RDFNode> {

        @Override
        public DataNode<RDFNode> call(final DataNodeList<RDFNode> param) {
            return null;
        }
    }

    private static class DefaultFirstAmbiguitySolver implements AmbiguitySolver<RDFNode> {

        @Override
        public DataNode<RDFNode> call(final DataNodeList<RDFNode> dataNodeList) {
            for (DataNode<RDFNode> dataNode : dataNodeList) {
                if (dataNode.data()
                            .isURIResource()) {
                    return dataNode;
                }
            }
            return null;
        }
    }

    /**
     * Performs a DFS on the given model, given selector, and a previous link
     *
     * @param model    the model
     * @param selector the selector
     */
    private <T extends RDFNode> void bfs(final Model model, final Selector selector, final DataNodeFactory<T> nodeFactory,
                                         final DataNodeRoot<T> root,
                                         final AmbiguitySolver<T> ambiguitySolver) {
        // list all statements based on the selector
        // only one statement should be found based on that selector
        // FIXME: this creates a list for no reason, just iterate
        // TODO: if the list size of nodes that meet requirements is 1 connect the node to the root
        // otherwise connect it to the previous node
        final DataNode<T> curr = nodeFactory.newNode((T) selector.getSubject());
        // TODO: meetsRequirements
        // it reads the resource inside, that's why we don't use it here
        root.addChild(curr);
        Platform.runLater(() -> {
            request.getObservableList()
                   .add(curr.data());
        });
        final List<Statement> statements = model.listStatements(selector)
                                                .toList();
        statements.sort((x, y) -> Boolean.compare(x.getObject()
                                                   .isURIResource(),
                                                  y.getObject()
                                                   .isURIResource()));
        final DataNodeList<T> children = new DataNodeList<>();
        for (final Statement stmt : statements) {
            final RDFNode next = stmt.getObject();
            // check whether the next meets requirements (i.e. check restrictions)
            if (!meetsRequirements(model, next)) {
                return;
            }

            final DataNode<T> nextNode = nodeFactory.newNode((T) next);
            L.debug("Found {}", next);
            children.add(nextNode);

            Platform.runLater(() -> {
                request.getObservableList()
                       .add(next);
            });
        }

        // no nodes found, stop searching
        if (children.isEmpty()) {
            return;
        }

        // only one found, that means it's going linearly
        // pass in this call's parent
        final DataNode<T> parent;
        if (children.size() == 1) {
            parent = root;
        } else {
            parent = curr;
        }
        parent.addChildren(children);

        // multiple children found, that means we need to branch out
        final DataNode<T> next = ambiguitySolver.call(children);
        if (next != null) {
            searchFurther(model, nodeFactory, root, next, ambiguitySolver);
        } else {
            for (DataNode<T> child : children) {
                searchFurther(model, nodeFactory, root, child, ambiguitySolver);
            }
        }
    }

    private <T extends RDFNode> void searchFurther(final Model model, final DataNodeFactory<T> nodeFactory, final DataNodeRoot<T> root,
                                                   final DataNode<T> next, final AmbiguitySolver<T> ambiguitySolver) {
        final T data = next.data();
        if (!data.isURIResource()) {
            return;
        }
        final Resource resource = (Resource) data;
        readResource(model, resource);
        if (usedURIs.add(resource.getURI())) {
            final Selector sel = getSelector(resource, model.getProperty(request.getNamespace(), request.getLink()));
            bfs(model, sel, nodeFactory, root, ambiguitySolver);
        }
    }

    private boolean meetsRequirements(final Model model, final RDFNode object) {
        if (!object.isURIResource()) {
            return true;
        }

        final Resource resource = object.asResource();
        readResource(model, resource);

        // check for the restrictions on the given request
        for (final Restriction r : request.getRestrictions()) {
            final Selector sel = getSelector(resource, model.getProperty(r.getKey(), r.getValue()));

            // a statement with the given restriction was not found -> they were not met
            if (!model.listStatements(sel)
                      .hasNext()) {
                // don't continue in the search
                return false;
            }
        }
        return true;
    }

    private void readResource(final Model model, final Resource resource) {
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
