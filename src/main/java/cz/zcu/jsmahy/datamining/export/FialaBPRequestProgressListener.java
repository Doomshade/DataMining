package cz.zcu.jsmahy.datamining.export;

import com.google.inject.Inject;
import cz.zcu.jsmahy.datamining.Main;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.util.SearchDialog;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.scene.web.WebView;
import org.apache.jena.rdf.model.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;
import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_RELATIONSHIPS;
import static cz.zcu.jsmahy.datamining.export.FialaBPMetadataKeys.*;
import static java.util.Objects.requireNonNull;

public class FialaBPRequestProgressListener implements RequestProgressListener {
    public static final Map<String, String> METADATA_DEFAULT_PROPERTIES = Map.of("startPrecision", "day", "endPrecision", "day");
    public static final String METADATA_DEFAULT_STEREOTYPE = "person";
    private static final Logger LOGGER = LogManager.getLogger(FialaBPRequestProgressListener.class);
    // valid values: "part_of", "creation", "participation", "cause", "takes_place", "interaction", "relationship"
    private static final String DEFAULT_STEREOTYPE = "relationship";
    private final ObjectProperty<Property> ontologyPathPredicate = new SimpleObjectProperty<>();
    private final ObjectProperty<Property> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<Property> endDate = new SimpleObjectProperty<>();
    private final ObjectProperty<TreeItem<DataNode>> treeRoot = new SimpleObjectProperty<>();
    private final ObjectProperty<QueryData> queryData = new SimpleObjectProperty<>();

    @Inject
    private SparqlEndpointAgent<?> sparqlEndpointAgent;
    @Inject
    private SparqlQueryServiceHolder serviceHolder;

    private void findTreeItem(DataNode dataNode, TreeItem<DataNode> currTreeItem, AtomicReference<TreeItem<DataNode>> ref) {
        for (final TreeItem<DataNode> child : currTreeItem.getChildren()) {
            final long childId = child.getValue()
                                      .getId();
            if (childId == dataNode.getId()) {
                ref.set(child);
                return;
            }
            if (!child.getChildren()
                      .isEmpty()) {
                findTreeItem(dataNode, child, ref);
            }
        }
    }

    // TODO: test this method!
    public TreeItem<DataNode> findTreeItem(final DataNode dataNode) {
        requireNonNull(dataNode);
        requireNonNull(treeRoot.get());

        // first iterate over all of root's children, then recursively check for the children of each node
        // if we don't find anything after the last element of the root's children no such tree item was found
        // this might look duplicate in the helper method, but we need to ensure that the inner children don't throw an exception in the
        // inner loops because they aren't required to have the tree item present, whereas the root is
        // thus the check at the end of this loop and no check in the helper recursive method (i.e. if we threw the exception in the
        // inner loop the search would terminate prematurely; we need to terminate after the last of root's children is checked)
        final AtomicReference<TreeItem<DataNode>> ref = new AtomicReference<>();
        for (final TreeItem<DataNode> child : treeRoot.get()
                                                      .getChildren()) {
            if (ref.get() != null) {
                break;
            }
            final long childId = child.getValue()
                                      .getId();
            if (childId == dataNode.getId()) {
                ref.set(child);
                break;
            }
            if (!child.getChildren()
                      .isEmpty()) {
                findTreeItem(dataNode, child, ref);
            }
        }
        final TreeItem<DataNode> treeItem = ref.get();
        if (treeItem == null) {
            throw new NoSuchElementException(String.format("Data node %s not found.", dataNode));
        }
        return treeItem;
    }

    @Override
    public ObjectProperty<Property> ontologyPathPredicateProperty() {
        return ontologyPathPredicate;
    }

    @Override
    public ObjectProperty<Property> startDateProperty() {
        return startDate;
    }

    @Override
    public ObjectProperty<Property> endDateProperty() {
        return endDate;
    }

    @Override
    public ObjectProperty<TreeItem<DataNode>> treeRootProperty() {
        return treeRoot;
    }

    @Override
    public ObjectProperty<QueryData> queryDataProperty() {
        return queryData;
    }

    @Override
    public void onAddRelationship(final DataNode prev, final DataNode curr) {
        // TODO: Let user set this stereotype, but default to person
        curr.addMetadata(METADATA_KEY_STEREOTYPE, METADATA_DEFAULT_STEREOTYPE);
        // TODO: Let user choose the date type, but default to day
        curr.addMetadata(METADATA_KEY_PROPERTIES, new HashMap<>(METADATA_DEFAULT_PROPERTIES));

        // If the previous data node is null we can't add any relationships
        if (prev == null) {
            return;
        }

        final List<ArbitraryDataHolder> relationships = curr.getValue(METADATA_KEY_RELATIONSHIPS, new ArrayList<>());
        // TODO: Relationship can go the opposite way
        // For now leave it like this
        final ArbitraryDataHolder relationship = new DefaultArbitraryDataHolder();
        relationship.addMetadata(METADATA_KEY_FROM, curr.getId());
        relationship.addMetadata(METADATA_KEY_TO, prev.getId());
        relationship.addMetadata(METADATA_KEY_NAME,
                                 queryData.get()
                                          .getOntologyPathPredicate()
                                          .getLocalName());

        // TODO: User input
        relationship.addMetadata(METADATA_KEY_STEREOTYPE, DEFAULT_STEREOTYPE);
        relationships.add(relationship);
        if (!curr.hasMetadataKey(METADATA_KEY_RELATIONSHIPS)) {
            curr.addMetadata(METADATA_KEY_RELATIONSHIPS, relationships);
        }
    }

    @Override
    public void onAddNewDataNodes(final List<DataNode> newDataNodes) {
        // the passed list should not be null nor empty
        assert newDataNodes != null && !newDataNodes.isEmpty();
        final DataNode first = newDataNodes.iterator()
                                           .next();
        final DataNode parent = first.getParent();

        // the parent should not be null as the passed data nodes should not be roots
        assert parent != null : String.format("%s does not have a parent (it's likely root).", first);

        LOGGER.trace("Adding multiple data nodes '{}' under '{}'", newDataNodes, parent);

        Platform.runLater(() -> {
            final TreeItem<DataNode> treeItem = findTreeItem(parent);
            treeItem.getChildren()
                    .addAll(newDataNodes.stream()
                                        .map(TreeItem::new)
                                        .toList());
            treeItem.setExpanded(true);
        });
    }

    @Override
    public void onDeleteDataNodes(final Collection<DataNode> selectedDataNodes) {
        // create a copy of the list because we modify the inner list which would throw an IOOBE otherwise
        // we iterate through the children of the root, and at the same time we delete
        // some children
        for (DataNode dataNode : selectedDataNodes) {
            deleteDataNode(dataNode);
        }
    }

    @Override
    public void onAddMultipleDataNodes(final DataNode dataNodesParent, final List<DataNode> dataNodes) {
        LOGGER.trace("Adding multiple data nodes '{}' under '{}'", dataNodes, dataNodesParent);
        Platform.runLater(() -> {
            final TreeItem<DataNode> treeItem = findTreeItem(dataNodesParent);
            treeItem.getChildren()
                    .addAll(dataNodes.stream()
                                     .map(TreeItem::new)
                                     .toList());
            treeItem.setExpanded(true);
        });
    }

    @Override
    public void onInvalidQuery(final String invalidQuery, final InitialSearchResult result) {
        Platform.runLater(() -> {
            // TODO: resource bundle
            assert result != InitialSearchResult.OK;
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
            switch (result) {
                case SUBJECT_NOT_FOUND -> {
                    alert.setTitle(resourceBundle.getString("alert-invalid-query-subject-not-found-title"));
                    alert.setHeaderText(resourceBundle.getString("alert-invalid-query-subject-not-found-header"));
                    alert.contentTextProperty()
                         .bind(Bindings.format(resourceBundle.getString("alert-invalid-query-subject-not-found-content"), invalidQuery));
                }
                case START_DATE_NOT_SELECTED -> {
                    alert.setTitle(resourceBundle.getString("alert-start-date-not-selected-title"));
                    alert.setHeaderText(resourceBundle.getString("alert-start-date-not-selected-header"));
                    alert.setContentText(resourceBundle.getString("alert-start-date-not-selected-content"));
                }
                case PATH_NOT_SELECTED -> {
                    alert.setTitle(resourceBundle.getString("alert-path-not-selected-title"));
                    alert.setHeaderText(resourceBundle.getString("alert-path-not-selected-header"));
                    alert.setContentText(resourceBundle.getString("alert-path-not-selected-content"));
                }
                case UNKNOWN -> {
                    alert.setTitle(resourceBundle.getString("alert-unknown-error-title"));
                    alert.setHeaderText(resourceBundle.getString("alert-unknown-error-header"));
                    alert.contentTextProperty()
                         .bind(Bindings.format(resourceBundle.getString("alert-unknown-error-content"), invalidQuery));
                }
                default -> throw new UnsupportedOperationException("Result type not handled: " + result);
            }
            alert.show();
        });
    }

    @Override
    public void onSearchDone(final DataNode dataNodeRoot) {
        Platform.runLater(() -> {
            assert dataNodeRoot.isRoot();
            final Alert continuePrompt = new Alert(Alert.AlertType.CONFIRMATION);
            continuePrompt.initOwner(Main.getPrimaryStage());
            continuePrompt.setTitle("Pokračovat");
            continuePrompt.setHeaderText("Přejete si pokračovat v hledání?");
            continuePrompt.setContentText("Potvrďte prosím, zda si přejete pokračovat v hledání");
            continuePrompt.showAndWait()
                          .ifPresent(buttonType -> {
                              if (buttonType == ButtonType.OK) {
                                  final ResourceBundle lang = ResourceBundle.getBundle("lang");
                                  final SearchDialog dialog = new SearchDialog(lang.getString("search-subject"), lang.getString("subject-to-search"));
                                  dialog.showAndWait()
                                        .ifPresent(searchValue -> {
                                            final String query = searchValue.replaceAll(" ", "_");
                                            if (query.isBlank()) {
                                                // TODO: show an alert
                                                return;
                                            }
                                            final Service<?> service = sparqlEndpointAgent.createBackgroundQueryService(query, dataNodeRoot);
                                            serviceHolder.bindQueryService(service);
                                            service.restart();
                                        });
                              }
                          });
        });
    }

    @Override
    public void onCreateNewRoot(final DataNode newDataNodeRoot) {
        final TreeItem<DataNode> treeRoot = this.treeRoot.get();
        assert newDataNodeRoot.isRoot(); // the data node should really be a root
        assert treeRoot != null;         // and the tree root should be set
        treeRoot.getChildren()
                .add(new TreeItem<>(newDataNodeRoot));
    }

    @Override
    public void onDisplayRequest(final DataNode dataNodeRoot, final WebView webView, final File topLevelFrontendDirectory) {
        final File fialaBPIndexFile = new File(topLevelFrontendDirectory, "fiala-bp/src/index.html");
//        LOGGER.info("Loading Fiala BP index.html: {}", fialaBPIndexFile);
//        final WebEngine engine = webView.getEngine();
        final String url = "file://" + fialaBPIndexFile.getAbsolutePath();
//        engine.load(url);
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(fialaBPIndexFile.toURI());
            } catch (IOException e) {
                LOGGER.error(e);
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
    }

    private void deleteDataNode(final DataNode dataNode) {
        Platform.runLater(() -> {
            try {
                final TreeItem<DataNode> treeItem = findTreeItem(dataNode);
                treeItem.getParent()
                        .getChildren()
                        .remove(treeItem);
            } catch (NoSuchElementException ignored) {
                // the tree item could be removed and the link to the data node removed, thus it's possible we fail to find a tree item with that node
                // graphical representation:
                //  R
                //  ├── A
                //  ├── B
                //  │   ├── C
                //  │   ├── D
                //  │   └── E
                //  ├── F
                //  │   └── G
                //  └── H
                // if we delete node B or F, the nodes C, D, E or G are no longer reachable by the parent
            }
        });
        // delete all relationships pointing to this datanode
        // we start from root and then check for all the data nodes
        // TODO: this could definitely be optimized, but it should not be that expensive anyways
        // also yes, we could use the ifPresent(Consumer) methods, but I found that unreadable
        final long id = dataNode.getId();
        final Optional<? extends DataNode> rootOpt = dataNode.findRoot();
        if (rootOpt.isEmpty()) {
            return;
        }

        // now iterate through all the items of the root, and for each check if it has a relationship
        // if it has a relationship, check if the "to" value points to this data node
        // if it does, remove the relationship
        final DataNode root = rootOpt.get();
        root.iterate((child, depth) -> {
            if (!child.hasMetadataKey(METADATA_KEY_RELATIONSHIPS)) {
                return;
            }
            if (child.getValueUnsafe(METADATA_KEY_RELATIONSHIPS) instanceof List<?> relationships) {
                final Iterator<?> it = relationships.iterator();
                while (it.hasNext()) {
                    Object obj = it.next();
                    if (obj instanceof ArbitraryDataHolder relationship) {
                        if (relationship.getValue(METADATA_KEY_TO, Long.MIN_VALUE) == id) {
                            it.remove();
                        }
                    }
                }
            }
        });
    }
}
