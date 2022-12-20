package cz.zcu.jsmahy.datamining.query.handlers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.zcu.jsmahy.datamining.api.AmbiguitySolver;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeListCellFactory;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import cz.zcu.jsmahy.datamining.query.AbstractRequestHandler;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.Restriction;
import cz.zcu.jsmahy.datamining.query.SparqlRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import lombok.NonNull;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * The DBPedia {@link RequestHandler}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaRequestHandler<T extends RDFNode> extends AbstractRequestHandler<T, Void> {
    private static final Logger L = LogManager.getLogger(DBPediaRequestHandler.class);
    private static final String DBPEDIA_SITE = "http://dbpedia.org/resource/";
    private static final Comparator<Statement> STATEMENT_COMPARATOR = (x, y) -> Boolean.compare(x.getObject()
                                                                                                 .isURIResource(),
                                                                                                y.getObject()
                                                                                                 .isURIResource());
    private static boolean requesting = false;
    private final Collection<String> usedURIs = new HashSet<>();
    private SparqlRequest<T> request = null;

    private final DataNodeFactory<T> nodeFactory;
    private final AmbiguitySolver<T> ambiguitySolver;

    {
        final Injector injector = Guice.createInjector(new DBPediaModule());
        injector.injectMembers(this);
        nodeFactory = injector.getInstance(DataNodeFactory.class);
        ambiguitySolver = new UserAmbiguitySolver();
    }


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
    protected synchronized Void internalQuery(@NonNull final SparqlRequest<T> request) throws InvalidQueryException {
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

        // prepare the fields, don't put them as parameters, it will just
        // fill stack with duplicates
        this.request = request;
        this.usedURIs.clear();

        // now iterate recursively
        L.debug("Searching...");

        final DataNodeRoot<T> dataNodeRoot = request.getDataNodeRoot();
        final TreeItem<T> treeRoot = request.getTreeRoot();
        final ObservableList<DataNode<T>> dataNodeRootChildren = dataNodeRoot.getChildren();
        final ObservableList<TreeItem<T>> treeRootChildren = treeRoot.getChildren();
        treeRootChildren.addListener(new TreeItemListChangeListener(dataNodeRootChildren));
        bfs(model, selector, nodeFactory, dataNodeRoot, treeRoot, ambiguitySolver);
        L.debug("Done searching");
        requesting = false;
        return null;
    }

    /**
     * Performs a DFS on the given model, given selector, and a previous link
     *
     * @param model    the model
     * @param selector the selector
     */
    private void bfs(final Model model, final Selector selector, final DataNodeFactory<T> nodeFactory, final DataNodeRoot<T> root, final TreeItem<T> treeRoot,
                     final AmbiguitySolver<T> ambiguitySolver) {
        // add the current node to the tree node
        final DataNode<T> curr = nodeFactory.newNode((T) selector.getSubject());
        final ObservableList<TreeItem<T>> treeChildren = treeRoot.getChildren();
        Platform.runLater(() -> treeChildren.add(new TreeItem<>(curr.getData())));

        final List<Statement> statements = model.listStatements(selector)
                                                .toList();
        statements.sort(STATEMENT_COMPARATOR);

        final ObservableList<DataNode<T>> children = FXCollections.observableArrayList();
        T previous = null;
        for (final Statement stmt : statements) {
            // check whether the next meets requirements (i.e. check restrictions)
            // and update previous node
            // we keep previous node to undo the jump to the resource that the meetsRequirements method makes
            final T next = (T) stmt.getObject();
            final boolean meetsRequirements;
            try {
                meetsRequirements = meetsRequirements(model, previous, next);
            } catch (AssertionError e) {
                L.error("An internal error occurred when trying to check for the requirements of the node {}.", next, e);
                return;
            }
            previous = next;
            if (!meetsRequirements) {
                return;
            }

            final DataNode<T> nextNode = nodeFactory.newNode(next);
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
            Platform.runLater(() -> {
                final DataNode<T> first = children.get(0);
                final T data = first.getData();
                for (TreeItem<T> item : treeChildren) {
                    if (item.getValue()
                            .equals(data)) {
                        return;
                    }
                }
                treeChildren.add(new TreeItem<>(data));
            });
            searchFurther(model, nodeFactory, root, children.get(0), treeRoot, ambiguitySolver);
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
            searchFurther(model, nodeFactory, root, next.get(), treeRoot, ambiguitySolver);
            return;
        }

        // otherwise search through the children and add those nodes to the current node that acts as a parent
        Platform.runLater(() -> {
            final int lastIndex = treeChildren.size() - 1;
            // TODO: the ambiguity can occur in the root perhaps?
            if (lastIndex < 0) {
                return;
            }
            treeChildren.get(lastIndex)
                        .getChildren()
                        .addAll(children.stream()
                                        .map(TreeItem::new)
                                        .collect(() -> FXCollections.observableArrayList(), (x, y) -> {
                                            x.add(new TreeItem<>(y.getValue()
                                                                  .getData()));
                                        }, List::addAll));
        });
        for (final DataNode<T> child : children) {
            searchFurther(model, nodeFactory, root, child, treeRoot, ambiguitySolver);
        }
    }

    private void searchFurther(final Model model, final DataNodeFactory<T> nodeFactory, final DataNodeRoot<T> root, final DataNode<T> next, final TreeItem<T> treeRoot,
                               final AmbiguitySolver<T> ambiguitySolver) {
        final T data = next.getData();
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

    /**
     * <p>Checks whether the {@code curr}ent node meets the requirements to be added to the search list.</p>
     * <p><b>IMPORTANT:</b> this method reads the resource to the model and <b>STAYS</b> there if this method returns {@code true}</p>
     * <p>If this method returns {@code false} it rolls back to the {@code previous} node.</p>
     *
     * @param model    the current model
     * @param previous the previous node
     * @param curr     the current node
     *
     * @return Whether the current node meets requirements. Read this method's javadoc for more information regarding the changes this method potentially makes to the model.
     *
     * @throws IllegalStateException if the previous is not a URI resource
     */
    private boolean meetsRequirements(final Model model, final T previous, final T curr) throws IllegalArgumentException {
        if (!curr.isURIResource()) {
            return true;
        }

        final Resource resource = curr.asResource();
        readResource(model, resource);

        // check for the restrictions on the given request
        for (final Restriction restriction : request.getRestrictions()) {
            final Selector sel = getSelector(resource, model.getProperty(restriction.getKey(), restriction.getValue()));

            // a statement with the given restriction was not found -> they were not met
            if (!model.listStatements(sel)
                      .hasNext()) {
                // stop the search
                // and go back to the previous node
                if (previous != null) {
                    readResource(model, previous.asResource());
                }
                return false;
            }
        }
        return true;
    }

    private void readResource(final Model model, final Resource resource) {
        String URI = resource.getURI();
        model.read(URI);
    }

    private class UserAmbiguitySolver implements AmbiguitySolver<T> {

        @Override
        public AtomicReference<DataNode<T>> call(final ObservableList<DataNode<T>> list) {
            AtomicReference<DataNode<T>> ref = new AtomicReference<>();
            Platform.runLater(() -> new DialogueHandler(list, ref).showDialogueAndWait());
            return ref;
        }

        private class DialogueHandler {
            private final Dialog<DataNode<T>> node = new Dialog<>();
            private final DialogPane dialogPane = node.getDialogPane();
            private final AtomicReference<DataNode<T>> ref;
            private final ListView<DataNode<T>> content = new ListView<>();

            {
                dialogPane.getButtonTypes()
                          .addAll(ButtonType.OK, ButtonType.CANCEL);
                content.setCellFactory(x -> new RDFNodeListCellFactory<>());
                node.setResultConverter(buttonType -> content.getSelectionModel()
                                                             .getSelectedItem());
            }

            public DialogueHandler(final ObservableList<DataNode<T>> list, final AtomicReference<DataNode<T>> ref) {
                this.ref = ref;
                this.content.setItems(list);
                this.dialogPane.setContent(content);
            }

            public void showDialogueAndWait() {
                // show the dialogue and wait for response
                ref.set(node.showAndWait()
                            .orElse(null));

                // once we receive the response notify the thread under the request handler's monitor
                // that we got a response from the user
                // the thread waits otherwise for another 5 seconds
                synchronized (DBPediaRequestHandler.this) {
                    DBPediaRequestHandler.this.notify();
                }
            }
        }
    }

    private class DefaultAllAmbiguitySolver implements AmbiguitySolver<T> {

        @Override
        public AtomicReference<DataNode<T>> call(final ObservableList<DataNode<T>> dataNodeList) {
            return new AtomicReference<>(null);
        }
    }

    private class DefaultFirstAmbiguitySolver implements AmbiguitySolver<T> {

        @Override
        public AtomicReference<DataNode<T>> call(final ObservableList<DataNode<T>> dataNodeList) {
            final AtomicReference<DataNode<T>> ref = new AtomicReference<>();
            DataNode<T> result = null;
            for (DataNode<T> dataNode : dataNodeList) {
                result = dataNode;
                if (dataNode.getData()
                            .isURIResource()) {
                    break;
                }
            }
            ref.set(result);
            return ref;
        }
    }

    private class TreeItemListChangeListener implements ListChangeListener<TreeItem<T>> {

        private final ObservableList<DataNode<T>> dataNodeRootChildren;

        public TreeItemListChangeListener(final ObservableList<DataNode<T>> dataNodeRootChildren) {
            this.dataNodeRootChildren = dataNodeRootChildren;
        }

        private List<? extends DataNode<T>> mapToDataNode(Collection<? extends TreeItem<T>> collection) {
            return collection.stream()
                             .map(this::mapToDataNode)
                             .collect(Collectors.toList());
        }

        private DataNode<T> mapToDataNode(TreeItem<T> treeItem) {
            return nodeFactory.newNode(treeItem.getValue());
        }

        @Override
        public void onChanged(final Change<? extends TreeItem<T>> change) {
            while (change.next()) {
                if (change.wasPermutated()) {
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        final DataNode<T> tmp = dataNodeRootChildren.get(i);
                        final int permutation = change.getPermutation(i);
                        dataNodeRootChildren.set(i, dataNodeRootChildren.get(permutation));
                        dataNodeRootChildren.set(permutation, tmp);
                    }
                } else if (change.wasReplaced()) {
                    dataNodeRootChildren.removeAll(mapToDataNode(change.getRemoved()));
                    dataNodeRootChildren.addAll(mapToDataNode(change.getAddedSubList()));
                } else if (change.wasRemoved()) {
                    dataNodeRootChildren.removeAll(mapToDataNode(change.getRemoved()));
                } else if (change.wasAdded()) {
                    dataNodeRootChildren.addAll(mapToDataNode(change.getAddedSubList()));
                } else if (change.wasUpdated()) {
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        dataNodeRootChildren.add(mapToDataNode(change.getList()
                                                                     .get(i)));
                    }
                }
            }
        }
    }

}
