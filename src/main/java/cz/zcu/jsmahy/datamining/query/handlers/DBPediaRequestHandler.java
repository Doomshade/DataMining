package cz.zcu.jsmahy.datamining.query.handlers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeListCellFactory;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import cz.zcu.jsmahy.datamining.query.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * The DBPedia {@link RequestHandler}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaRequestHandler<T extends RDFNode> extends AbstractRequestHandler<T> {
    private static final Logger L = LogManager.getLogger(DBPediaRequestHandler.class);
    private static final String DBPEDIA_SITE = "http://dbpedia.org/resource/";
    private static boolean requesting = false;
    private final Collection<String> usedURIs = new HashSet<>();
    private Ontology currOntology = null;
    private Model model = null;
    private SparqlRequest<T> request = null;


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
    protected synchronized Ontology query0(final SparqlRequest<T> request) throws InvalidQueryException {
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
        final DataNodeFactory<T> nodeFactory = injector.getInstance(DataNodeFactory.class);
        final DataNodeRoot<T> root = nodeFactory.newRoot();
        final AmbiguitySolver<T> ambiguitySolver = new UserAmbiguitySolver();
        bfs(model, selector, nodeFactory, root, request.getRoot(), ambiguitySolver);
        L.debug("Done searching");
        requesting = false;
        return ontology;
    }

    /**
     * Performs a DFS on the given model, given selector, and a previous link
     *
     * @param model    the model
     * @param selector the selector
     */
    private void bfs(final Model model, final Selector selector, final DataNodeFactory<T> nodeFactory, final DataNodeRoot<T> root, final TreeItem<T> treeRoot,
                     final AmbiguitySolver<T> ambiguitySolver) {
        // list all statements based on the selector
        // only one statement should be found based on that selector
        // FIXME: this creates a list for no reason, just iterate
        // otherwise connect it to the previous node
        final DataNode<T> curr = nodeFactory.newNode((T) selector.getSubject());
        // TODO: meetsRequirements
        // it reads the resource inside, that's why we don't use it here
        root.addChild(curr);
        final ObservableList<TreeItem<T>> treeChildren = treeRoot.getChildren();
        Platform.runLater(() -> {
            treeChildren.add(new TreeItem<>(curr.data()));
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
        }

        // no nodes found, stop searching
        if (children.isEmpty()) {
            return;
        }

        // only one found, that means it's going linearly
        // pass in this call's parent
        if (children.size() == 1) {
            root.addChildren(children);
            Platform.runLater(() -> {
                final DataNode<T> first = children.get(0);
                final T data = first.data();
                for (TreeItem<T> item : treeChildren) {
                    if (item.getValue()
                            .equals(data)) {
                        return;
                    }
                }
                treeChildren.add(new TreeItem<>(data));
            });
            return;
        }
        // multiple children found, that means we need to branch out
        // the ambiguity solver might pop up a dialogue where it could wait
        // for the response of the user
        // the dialogue is then responsible for notifying the monitor of this object
        // to free this thread
        // the thread will wait up to 5 seconds and check for the result if the
        // dialogue fails to notify the monitor
        final AtomicReference<DataNode<T>> next = ambiguitySolver.call(children);
        while (next.get() == null) {
            try {
                wait(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // if a node was chosen search further down that node
        if (next.get() != null) {
            root.addChild(next.get());
            searchFurther(model, nodeFactory, root, next.get(), treeRoot, ambiguitySolver);
            return;
        }

        // otherwise search through the children and add those nodes to the current node that acts as a parent
        curr.addChildren(children);
        Platform.runLater(() -> {
            final int lastIndex = treeChildren.size() - 1;
            if (lastIndex < 0) {
                return;
            }
            treeChildren.get(lastIndex)
                        .getChildren()
                        .addAll(children.stream()
                                        .map(TreeItem::new)
                                        .collect(() -> FXCollections.observableArrayList(), (x, y) -> {
                                            x.add(new TreeItem<>(y.getValue()
                                                                  .data()));
                                        }, List::addAll));
        });
        for (DataNode<T> child : children) {
            searchFurther(model, nodeFactory, root, child, treeRoot, ambiguitySolver);
        }
    }

    private void searchFurther(final Model model, final DataNodeFactory<T> nodeFactory, final DataNodeRoot<T> root, final DataNode<T> next, final TreeItem<T> treeRoot,
                               final AmbiguitySolver<T> ambiguitySolver) {
        final T data = next.data();
        if (!data.isURIResource()) {
            return;
        }
        final Resource resource = (Resource) data;
        readResource(model, resource);
        if (usedURIs.add(resource.getURI())) {
            final Selector sel = getSelector(resource, model.getProperty(request.getNamespace(), request.getLink()));
            bfs(model, sel, nodeFactory, root, treeRoot, ambiguitySolver);
        }
    }

    private boolean meetsRequirements(final Model model, final RDFNode object) {
        if (!object.isURIResource()) {
            return true;
        }

        final Resource resource = object.asResource();
        readResource(model, resource);

        // check for the restrictions on the given request
        for (final Restriction restriction : request.getRestrictions()) {
            final Selector sel = getSelector(resource, model.getProperty(restriction.getKey(), restriction.getValue()));

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

    private class UserAmbiguitySolver implements AmbiguitySolver<T> {

        @Override
        public AtomicReference<DataNode<T>> call(final DataNodeList<T> list) {
            AtomicReference<DataNode<T>> ref = new AtomicReference<>();
            Platform.runLater(() -> {
                // prepare the dialogue
                final Dialog<DataNode<T>> node = new Dialog<>();
                final DialogPane dialogPane = node.getDialogPane();
                dialogPane.getButtonTypes()
                          .addAll(ButtonType.OK, ButtonType.CANCEL);

                final ObservableList<DataNode<T>> dataNodes = FXCollections.observableArrayList(list);
                final ListView<DataNode<T>> content = new ListView<>(dataNodes);
                content.setCellFactory(x -> new RDFNodeListCellFactory<>());
                dialogPane.setContent(content);
                node.setResultConverter(buttonType -> content.getSelectionModel()
                                                             .getSelectedItem());

                // show the dialogue and wait for response
                ref.set(node.showAndWait()
                            .orElse(null));

                // once we receive the response notify the thread under the request handler's monitor
                // see bfs method
                synchronized (DBPediaRequestHandler.this) {
                    DBPediaRequestHandler.this.notify();
                }
            });
            return ref;
        }
    }

    private class DefaultAllAmbiguitySolver implements AmbiguitySolver<T> {

        @Override
        public AtomicReference<DataNode<T>> call(final DataNodeList<T> dataNodeList) {
            return new AtomicReference<>(null);
        }
    }

    private class DefaultFirstAmbiguitySolver implements AmbiguitySolver<T> {

        @Override
        public AtomicReference<DataNode<T>> call(final DataNodeList<T> dataNodeList) {
            final AtomicReference<DataNode<T>> ref = new AtomicReference<>();
            DataNode<T> result = null;
            for (DataNode<T> dataNode : dataNodeList) {
                result = dataNode;
                if (dataNode.data()
                            .isURIResource()) {
                    break;
                }
            }
            ref.set(result);
            return ref;
        }
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
