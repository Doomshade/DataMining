package cz.zcu.jsmahy.datamining.query.handlers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import cz.zcu.jsmahy.datamining.query.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The DBPedia {@link RequestHandler}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaRequestHandler<T extends RDFNode, R extends Void> extends AbstractRequestHandler<T, R> implements BlockingRequestHandler<T, R> {
    //<editor-fold desc="Constants">
    private static final String NAMESPACE = "http://dbpedia.org/ontology/";
    private static final String LINK = "doctoralAdvisor";
    private static final Logger LOGGER = LogManager.getLogger(DBPediaRequestHandler.class);
    private static final String DBPEDIA_SITE = "http://dbpedia.org/resource/";
    /**
     * <p>This comparator ensures the URI resources are placed first over literal resources.</p>
     * <p>The reasoning behind is simple: if the user wishes to fast forward when searching down the line we need to iterate through URI resources first.</p>
     */
    private static final Comparator<Statement> STATEMENT_COMPARATOR = (x, y) -> Boolean.compare(x.getObject()
                                                                                                 .isURIResource(),
                                                                                                y.getObject()
                                                                                                 .isURIResource());
    //</editor-fold>

    private static boolean requesting = false;
    private final Collection<String> usedURIs = new HashSet<>();

    private final DataNodeFactory<T> nodeFactory;

    private final AmbiguousInputResolver<T, R, ?> ambiguousInputResolver;

    private final AmbiguousInputResolver<T, R, ?> ontologyPathPredicateInputResolver;
    private final AmbiguousInputMetadata<T, R> inputMetadata = new AmbiguousInputMetadata<>();

    @SuppressWarnings("unchecked")
    public DBPediaRequestHandler() {
        final Injector injector = Guice.createInjector(new DBPediaModule());
        nodeFactory = injector.getInstance(DataNodeFactory.class);
        ambiguousInputResolver = injector.getInstance(UserAssistedAmbiguousInputResolver.class);
        ontologyPathPredicateInputResolver = injector.getInstance(OntologyPathPredicateInputResolver.class);
    }

    /**
     * Attempts to continue the search if the monitor is in wait queue.
     */
    public synchronized void unlockDialogPane() {
        notify();
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
    protected synchronized R internalQuery(final String query, final TreeItem<DataNode<T>> treeRoot) throws InvalidQueryException {
        if (requesting) {
            throw new IllegalStateException("Already requesting!");
        }
        requesting = true;

        // create the root request and model
        final String requestPage = DBPEDIA_SITE.concat(query);
        final OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        try {
            LOGGER.debug(String.format("Requesting %s", requestPage));
            model.read(requestPage);
        } catch (HttpException e) {
            requesting = false;
            throw LOGGER.throwing(e);
        } catch (Exception e) {
            requesting = false;
            throw new InvalidQueryException(e);
        }

        // root subject and predicate
        final Resource subject = model.getResource(requestPage);
        final Property predicate = model.getProperty(NAMESPACE, LINK);
        final Selector selector = getSelector(subject, predicate);

        // prepare the fields, don't put them as parameters, it will just
        // fill stack with duplicates
        this.inputMetadata.setRequestHandler(this);
        this.inputMetadata.setOntologyPathPredicate(predicate);
        this.inputMetadata.setRestrictions(new ArrayList<>());
        this.usedURIs.clear();

        // now iterate recursively
        LOGGER.debug("Searching...");

        if (initialSearch(subject, model, selector, nodeFactory, treeRoot)) {
            search(model, selector, nodeFactory, treeRoot);
            LOGGER.debug("Done searching");
        } else {
            LOGGER.debug("Invalid query '{}' - no results were found.", query);
            Platform.runLater(() -> {
                // TODO: resource bundle
                final Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid query");
                alert.setHeaderText("ERROR - Invalid query");
                final String wikiUrl = "https://en.wikipedia.org/wiki/";
                final String queryWikiUrl = wikiUrl + query;
                final String exampleWikiUrl = wikiUrl + "Charles_IV,_Holy_Roman_Emperor";
                final String exampleUri = "Charles IV, Holy Roman Emperor";
                alert.setContentText(String.format(
                        "No results were found querying '%s'. The query must correspond to the wikipedia URL:%n%n%s%n%nYour query corresponds to an unknown URL:%n%n%s%n%nIn this example '%s' is a " +
                        "valid query. Spaces instead of underscores are allowed.",
                        query,
                        exampleWikiUrl,
                        queryWikiUrl,
                        exampleUri));
//                alert.initOwner(Main.getPrimaryStage());
                alert.show();
            });
        }
        requesting = false;
        return null;
    }

    private static final Collection<String> IGNORED_PREDICATES = new ArrayList<>() {
        {
            add("http://dbpedia.org/ontology/wikiPageWikiLink");
            add("http://dbpedia.org/ontology/wikiPageExternalLink");
            add("http://www.w3.org/2002/07/owl#sameAs");
            add("http://dbpedia.org/property/wikiPageUsesTemplate");
            add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            add("http://dbpedia.org/ontology/abstract");
            add("http://purl.org/dc/terms/subject");
            add("http://www.w3.org/2000/01/rdf-schema#label");
            add("http://dbpedia.org/ontology/wikiPageRevisionID");
            add("http://xmlns.com/foaf/0.1/depiction");
            add("http://dbpedia.org/ontology/deathPlace");
            add("http://www.w3.org/2000/01/rdf-schema#comment");
            add("http://dbpedia.org/property/wikt");
            add("http://www.w3.org/2000/01/rdf-schema#comment");
            add("http://dbpedia.org/ontology/wikiPageLength");
            add("http://dbpedia.org/property/thesisTitle");
            add("http://dbpedia.org/ontology/thumbnail");
        }
    };

    private boolean initialSearch(final Resource subject, final Model model, final Selector selector, final DataNodeFactory<T> nodeFactory, final TreeItem<DataNode<T>> treeRoot) {
        final Selector s = new SimpleSelector(subject, null, null, "en") {
            @Override
            public boolean selects(final Statement s) {
                final Property predicate = s.getPredicate();
                for (String ignoredPredicate : IGNORED_PREDICATES) {
                    if (predicate.hasURI(ignoredPredicate)) {
                        return false;
                    }
                }
                return true;
            }
        };

        StmtIterator stmtIterator = model.listStatements(s);
        if (!stmtIterator.hasNext()) {
            return false;
        }
        final List<DataNode<T>> properties = stmtIterator.toList()
                                                         .stream()
                                                         .map(statement -> (T) statement.getPredicate())
                                                         .map(nodeFactory::newNode)
                                                         .toList();
        final DataNodeReferenceHolder<T> ref = ontologyPathPredicateInputResolver.resolveRequest(FXCollections.observableArrayList(properties), inputMetadata);
        if (ref instanceof BlockingDataNodeReferenceHolder<T> blockingRef) {
            while (!blockingRef.isFinished()) {
                try {
                    wait(5000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
//        final DataNodeReferenceHolder<T> ref = new DataNodeReferenceHolder<>();
//        final ObservableList<DataNode<T>> list = FXCollections.observableArrayList();
//        list.add(nodeFactory.newNode(test));
//        Platform.runLater(() -> {
//            final DialogHelper.ItemChooseDialog<T, R> dialog = helper.itemChooseDialog(ref, this, x -> new RDFNodeListCellFactory<>(), list, SelectionMode.SINGLE);
//            dialog.showDialogueAndWait();
//        });
        return true;
    }

    public static class OntologyPathPredicateInputResolver<T, R> implements BlockingAmbiguousInputResolver<T, R> {
        @Override
        public BlockingDataNodeReferenceHolder<T> resolveRequest(final ObservableList<DataNode<T>> ambiguousInput, final AmbiguousInputMetadata<T, R> inputMetadata) {
            if (ambiguousInput.isEmpty()) {
                throw new IllegalArgumentException("Ambiguous input cannot be empty.");
            }
            final DataNode<T> firstNode = ambiguousInput.get(0);
            if (!(firstNode.getData() instanceof Property)) {
                throw new IllegalArgumentException(String.format("Invalid input type. Received: %s. Expected: %s",
                                                                 firstNode.getData()
                                                                          .getClass(),
                                                                 Property.class));
            }

            final BlockingDataNodeReferenceHolder<T> ref = new BlockingDataNodeReferenceHolder<>();
            if (ambiguousInput.size() == 1) {
                ref.set(firstNode);
                ref.unlock();
                return ref;
            }

            Platform.runLater(() -> {
                final DialogWrapper dialog = new DialogWrapper(ref, ambiguousInput);
                dialog.showDialogueAndWait(inputMetadata.getRequestHandler());
            });
            return ref;
        }


        private class DialogWrapper {
            private static final ReadOnlyObjectWrapper<String> UNKNOWN_VALUE = new ReadOnlyObjectWrapper<>("Unknown value");

            private class PropertyModel extends TableRow<Property> {
                @Override
                protected void updateItem(final Property item, final boolean empty) {
                    super.updateItem(item, empty);
                }

            }

            private final Dialog<Property> dialog = new Dialog<>();
            private final DialogPane dialogPane = dialog.getDialogPane();
            private final TableView<Property> content;
            private final DataNodeReferenceHolder<T> ref;
            private final Map<String, Model> modelCache = new HashMap<>();

            private DialogWrapper(final DataNodeReferenceHolder<T> ref, final ObservableList<DataNode<T>> input) {
                final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
                this.ref = ref;
                this.content = new TableView<>();
                this.content.getItems()
                            .addAll(input.stream()
                                         .map(dataNode -> (Property) dataNode.getData())
                                         .toList());
//                this.content.setRowFactory(tv -> new PropertyModel());
                final TableColumn<Property, String> propertyColumn = new TableColumn<>();
                propertyColumn.setCellValueFactory(features -> {
                    final String uri = features.getValue()
                                               .getURI();
                    if (!uri.contains("dbpedia")) {
//                        features.getTableView()
//                                .getItems()
//                                .remove(features.getValue());
                        return null;
                    }
//
//                    if (!modelCache.containsKey(uri)) {
//                        final Service<Model> svc = new Service<>() {
//                            @Override
//                            protected Task<Model> createTask() {
//                                return new Task<>() {
//                                    @Override
//                                    protected Model call() throws Exception {
//                                        final OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
//                                        model.read(uri);
//                                        return model;
//                                    }
//                                };
//                            }
//                        };
//                        svc.setOnSucceeded(ev -> {
//                            final Model value = (Model) ev.getSource()
//                                                          .getValue();
//                            modelCache.put(uri, value);
//                        });
//                        svc.setOnFailed(ev -> {
//                            svc.getException()
//                               .printStackTrace();
//                            synchronized (this) {
//                                notify();
//                            }
//                        });
//                        svc.restart();
//                    }
//                    synchronized (this) {
//                        final long start = System.currentTimeMillis();
//                        while (!modelCache.containsKey(uri) || System.currentTimeMillis() - start >= 5000L) {
//                            try {
//                                wait(5L);
//                            } catch (InterruptedException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                    }
                    modelCache.computeIfAbsent(uri, val -> {
                        final OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
                        try {
                            model.read(val);
                        } catch (Exception e) {
                            LOGGER.throwing(e);
                        }
                        return model;
                    });

                    if (!modelCache.containsKey(uri)) {
                        LOGGER.info("(1) Failed to find " + uri);
//                        features.getTableView()
//                                .getItems()
//                                .remove(features.getValue());
                        return null;
                    }

                    final Model propertyModel = modelCache.get(uri);
                    final Property prop = propertyModel.getProperty("http://www.w3.org/2000/01/rdf-schema#", "label");
                    final Statement val = propertyModel.getProperty(features.getValue(), prop, "en");
                    if (val == null) {
                        LOGGER.info("(2) Failed to find " + uri);
//                        features.getTableView()
//                                .getItems()
//                                .remove(features.getValue());
                        return null;
                    }
                    return new ReadOnlyObjectWrapper<>(val.getObject()
                                                          .asLiteral()
                                                          .getString());
                });
                this.content.getColumns()
                            .add(propertyColumn);

                this.dialog.setResultConverter(buttonType -> {
                    if (buttonType == ButtonType.CANCEL) {
                        return null;
                    }
                    if (buttonType == ButtonType.OK) {
                        return content.getSelectionModel()
                                      .getSelectedItem();
                    }
                    LOGGER.error("Unrecognized button type: {}", buttonType);
                    return null;
                });
                this.dialogPane.getButtonTypes()
                               .addAll(ButtonType.OK, ButtonType.CANCEL);
                this.dialogPane.setContent(content);
            }


            public void showDialogueAndWait(final RequestHandler<T, R> requestHandler) {
                try {
                    final Optional<Property> property = dialog.showAndWait();
                    property.ifPresent(ref::setOntologyPathPredicate);
                } catch (Exception e) {
                    LOGGER.throwing(e);
                }
                if (ref instanceof BlockingDataNodeReferenceHolder<T> blockingRef) {
                    blockingRef.unlock();
                    requestHandler.unlockDialogPane();
                }
            }
        }

    }


    /**
     * Recursively searches based on the given model, selector, and a previous link. Adds the subject of the selector to the tree.
     *
     * @param model       the model
     * @param selector    the selector
     * @param nodeFactory the data node factory
     * @param treeRoot    the tree root
     */
    private void search(final Model model, final Selector selector, final DataNodeFactory<T> nodeFactory, final TreeItem<DataNode<T>> treeRoot) {
        inputMetadata.setModel(model);

        // add the current node to the tree node
        final DataNode<T> curr = nodeFactory.newNode((T) selector.getSubject());
        final ObservableList<TreeItem<DataNode<T>>> treeChildren = treeRoot.getChildren();
        final TreeItem<DataNode<T>> currTreeItem = new TreeItem<>(curr);
        Platform.runLater(() -> treeChildren.add(currTreeItem));

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
                LOGGER.error("An internal error occurred when trying to check for the requirements of the node {}.", next, e);
                return;
            }
            previous = next;
            if (!meetsRequirements) {
                return;
            }

            final DataNode<T> nextNode = nodeFactory.newNode(next);
            LOGGER.debug("Found {}", next);
            children.add(nextNode);
        }

        // no nodes found, stop searching
        if (children.isEmpty()) {
            return;
        }

        // only one found, that means it's going linearly
        // pass in this resolveRequest's parent
        if (children.size() == 1) {
            final DataNode<T> first = children.get(0);
            searchFurther(model, nodeFactory, first, treeRoot);
            return;
        }

        // multiple children found, that means we need to branch out
        // the ambiguity solver might pop up a dialogue where it could wait
        // for the response of the user
        // the dialogue is then responsible for notifying the monitor of this object
        // to free this thread via #unlockDialogPane method
        // the thread will wait up to 5 seconds and check for the result if the
        // dialogue fails to notify the monitor
        final DataNodeReferenceHolder<T> ref = ambiguousInputResolver.resolveRequest(children, inputMetadata);
        if (ref instanceof BlockingDataNodeReferenceHolder<T> blockingRef) {
            while (!blockingRef.isFinished()) {
                try {
                    wait(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // if a node was chosen search further down that node
        final boolean multipleReferences = ref.hasMultipleReferences();
        LOGGER.debug("Has multiple references: " + multipleReferences);
        LOGGER.debug("References: " + ref.getList());
        if (!multipleReferences) {
            searchFurther(model, nodeFactory, ref.get(), treeRoot);
        } else {
            final List<DataNode<T>> values = ref.getList();
            currTreeItem.getChildren()
                        .addAll(children.stream()
                                        .map(TreeItem::new)
                                        .toList());
            currTreeItem.setExpanded(true);
            // TODO: add another dialog to choose where to continue
            if (!values.isEmpty()) {
                searchFurther(model, nodeFactory, values.get(0), treeRoot);
            }
        }

//        for (final DataNode<T> child : children) {
//            searchFurther(model, nodeFactory, child, treeRoot);
//        }
    }

    private void searchFurther(final Model model, final DataNodeFactory<T> nodeFactory, final DataNode<T> next, final TreeItem<DataNode<T>> treeRoot) {
        final T data = next.getData();
        if (!data.isURIResource()) {
            return;
        }
        final Resource resource = (Resource) data;
        readResource(model, resource);
        if (usedURIs.add(resource.getURI())) {
            final Selector sel = getSelector(resource, model.getProperty(NAMESPACE, LINK));
            search(model, sel, nodeFactory, treeRoot);
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
        for (final Restriction restriction : inputMetadata.getRestrictions()) {
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
        String uri = resource.getURI();
        model.read(uri);
    }

    private class TreeItemListChangeListener implements ListChangeListener<TreeItem<DataNode<T>>> {

        private final ObservableList<DataNode<T>> dataNodeRootChildren;

        public TreeItemListChangeListener(final ObservableList<DataNode<T>> dataNodeRootChildren) {
            this.dataNodeRootChildren = dataNodeRootChildren;
        }

        private List<? extends DataNode<T>> mapToDataNode(Collection<? extends TreeItem<DataNode<T>>> collection) {
            return collection.stream()
                             .map(this::mapToDataNode)
                             .collect(Collectors.toList());
        }

        private DataNode<T> mapToDataNode(TreeItem<DataNode<T>> treeItem) {
            return nodeFactory.newNode(treeItem.getValue()
                                               .getData());
        }

        @Override
        public void onChanged(final Change<? extends TreeItem<DataNode<T>>> change) {
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
