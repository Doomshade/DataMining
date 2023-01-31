package cz.zcu.jsmahy.datamining.app.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jfoenix.controls.JFXSpinner;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import cz.zcu.jsmahy.datamining.dbpedia.DBPediaEndpointTask;
import cz.zcu.jsmahy.datamining.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.util.DialogHelper;
import cz.zcu.jsmahy.datamining.util.RDFNodeUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The controller for main UI where user builds the ontology.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class MainController<T extends RDFNode> implements Initializable, RequestProgressListener<T> {
    /*
PREFIX rdf: <https://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX r: <http://dbpedia.org/resource/>
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
select distinct ?name
{
?pred dbp:predecessor <http://dbpedia.org/resource/Charles_IV,_Holy_Roman_Emperor> .
?pred dbp:predecessor+ ?name
}
order by ?pred
     */
    private static final String WIKI_URL = "https://wikipedia.org/wiki/%s";
    private static final Logger LOGGER = LogManager.getLogger(MainController.class);
    private static MainController<?> instance = null;
    //<editor-fold desc="Attributes for query building">
    private final ObjectProperty<Property> ontologyPathPredicate = new SimpleObjectProperty<>();
    //<editor-fold desc="UI related attributes">
    @FXML
    private BorderPane rootPane;
    @FXML
    private TreeView<DataNode<T>> ontologyTreeView;
    @FXML
    private WebView wikiPageWebView;
    @FXML
    private JFXSpinner progressIndicator;
    //</editor-fold>
    private DataNodeFactory<T> nodeFactory;
    //</editor-fold>
    private DialogHelper dialogHelper;
    private final EventHandler<ActionEvent> createNewLineAction = e -> {
        final ResourceBundle lang = ResourceBundle.getBundle("lang");
        dialogHelper.textInputDialog(lang.getString("create-new-line"), lineName -> {
            final DataNodeRoot<T> dataNode = nodeFactory.newRoot(lineName);
            ontologyTreeView.getRoot()
                            .getChildren()
                            .add(new TreeItem<>(dataNode));
        }, "Title");

    };
    private SparqlEndpointAgent<T, Void> requestHandler;

    public static MainController<?> getInstance() {
        return instance;
    }

    // TODO: test this method!
    public static <T> TreeItem<DataNode<T>> findTreeItem(final DataNode<T> dataNode, final TreeItem<DataNode<T>> treeRoot) {
        Objects.requireNonNull(dataNode);
        Objects.requireNonNull(treeRoot);

        // first iterate over all of root's children, then recursively check for the children of each node
        // if we don't find anything after the last element of the root's children no such tree item was found
        // this might look duplicate in the helper method, but we need to ensure that the inner children don't throw an exception in the
        // inner loops because they aren't required to have the tree item present, whereas the root is
        // thus the check at the end of this loop and no check in the helper recursive method (i.e. if we threw the exception in the
        // inner loop the search would terminate prematurely; we need to terminate after the last of root's children is checked)
        final AtomicReference<TreeItem<DataNode<T>>> ref = new AtomicReference<>();
        for (final TreeItem<DataNode<T>> child : treeRoot.getChildren()) {
            if (ref.get() != null) {
                break;
            }
            final long childId = child.getValue()
                                      .getId();
            LOGGER.trace("Checking ID {}", childId);
            if (childId == dataNode.getId()) {
                LOGGER.trace("Found ID {}. Terminating.", childId);
                ref.set(child);
                break;
            }
            if (!child.getChildren()
                      .isEmpty()) {
                findTreeItem(dataNode, child, ref);
            }
        }
        final TreeItem<DataNode<T>> treeItem = ref.get();
        if (treeItem == null) {
            throw new NoSuchElementException(String.format("Data node %s not found.", dataNode));
        }
        return treeItem;
    }

    private static <T> void findTreeItem(DataNode<T> dataNode, TreeItem<DataNode<T>> currTreeItem, AtomicReference<TreeItem<DataNode<T>>> ref) {
        for (final TreeItem<DataNode<T>> child : currTreeItem.getChildren()) {
            final long childId = child.getValue()
                                      .getId();
            LOGGER.trace("Checking ID {}", childId);
            if (childId == dataNode.getId()) {
                LOGGER.trace("Found ID {}. Terminating.", childId);
                ref.set(child);
                return;
            }
            if (!child.getChildren()
                      .isEmpty()) {
                findTreeItem(dataNode, child, ref);
            }
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(final URL location, final ResourceBundle resources) {
        instance = this;
        final Injector injector = Guice.createInjector(new DBPediaModule());
        this.nodeFactory = injector.getInstance(DataNodeFactory.class);
        this.requestHandler = injector.getInstance(SparqlEndpointAgent.class);
        this.dialogHelper = injector.getInstance(DialogHelper.class);

        final MenuBar menuBar = createMenuBar(resources);
        this.rootPane.setTop(menuBar);
        this.rootPane.setPadding(new Insets(10));
        this.rootPane.disableProperty()
                     .bind(progressIndicator.visibleProperty());

        final MultipleSelectionModel<TreeItem<DataNode<T>>> selectionModel = this.ontologyTreeView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);

        // TODO: this will likely be a popup
        selectionModel.selectedItemProperty()
                      .addListener(this::onSelection);

        this.ontologyTreeView.setShowRoot(false);
        this.ontologyTreeView.getSelectionModel()
                             .setSelectionMode(SelectionMode.MULTIPLE);
        this.ontologyTreeView.setEditable(true);
        this.ontologyTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                event.consume();
            }
        });
        this.ontologyTreeView.setCellFactory(lv -> new RDFNodeCellFactory<>(lv, resources, this, dialogHelper, nodeFactory, requestHandler));

        final TreeItem<DataNode<T>> root = new TreeItem<>(null);
        this.ontologyTreeView.setRoot(root);
        final ContextMenu contextMenu = createContextMenu(resources);
        this.ontologyTreeView.setContextMenu(contextMenu);
    }

    private ContextMenu createContextMenu(final ResourceBundle resources) {
        final MenuItem addNewLineItem = createAddNewLineMenuItem(resources);

        return new ContextMenu(addNewLineItem);
    }

    private MenuBar createMenuBar(final ResourceBundle resources) {
        final MenuBar menuBar = new MenuBar();
        final Menu lineMenu = createLineMenu(resources);
        final Menu fileMenu = createFileMenu(resources);
        final Menu helpMenu = createHelpMenu(resources);

        menuBar.getMenus()
               .addAll(lineMenu, fileMenu, helpMenu);
        return menuBar;
    }

    private Menu createHelpMenu(final ResourceBundle resources) {
        final Menu helpMenu = new Menu(resources.getString("help"));
        final MenuItem tempMenuItem = new MenuItem("Empty :)");
        helpMenu.getItems()
                .addAll(tempMenuItem);
        return helpMenu;
    }

    private Menu createLineMenu(final ResourceBundle resources) {
        final Menu lineMenu = new Menu("_" + resources.getString("line"));
        final MenuItem newLineMenuItem = createAddNewLineMenuItem(resources);
        lineMenu.getItems()
                .addAll(newLineMenuItem);
        return lineMenu;
    }

    private Menu createFileMenu(final ResourceBundle resources) {
        final Menu fileMenu = new Menu("_" + resources.getString("file"));
        fileMenu.setMnemonicParsing(true);

        final MenuItem exportToFile = new MenuItem(resources.getString("export"));
        exportToFile.setAccelerator(KeyCombination.keyCombination("ALT + E"));
        fileMenu.getItems()
                .addAll(exportToFile);
        return fileMenu;
    }

    private MenuItem createAddNewLineMenuItem(final ResourceBundle resources) {
        final MenuItem menuItem = new MenuItem();
        menuItem.setText(resources.getString("create-new-line"));
        menuItem.setAccelerator(KeyCombination.keyCombination("CTRL + N"));
        menuItem.setOnAction(createNewLineAction);
        return menuItem;
    }

    /**
     * Handler for mouse press on the search button.
     *
     * @param root        the tree root
     * @param searchValue the search value
     */
    public void search(final DataNodeRoot<T> root, final String searchValue) {
        if (searchValue.isBlank()) {
            LOGGER.trace("Search field is blank, not searching for anything.");
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid search");
            alert.setContentText("Please enter some text to search.");
            return;
        }

        LOGGER.trace("Creating a background service for {} with root {}", searchValue, root);
        final Service<Void> query = createSearchService(root, searchValue);
        query.restart();
        bindService(query);
    }

    private Service<Void> createSearchService(final DataNodeRoot<T> root, final String searchValue) {
        final Service<Void> query = requestHandler.createBackgroundService(searchValue, root);
        query.setOnSucceeded(x -> ontologyTreeView.getSelectionModel()
                                                  .selectFirst());
        query.exceptionProperty()
             .addListener((observable, oldValue, newValue) -> {
                 if (newValue != null) {
                     newValue.printStackTrace();
                 }
             });
        return query;
    }

    public synchronized void bindService(final Service<?> service) {
        this.rootPane.disableProperty()
                     .bind(service.runningProperty());
        this.progressIndicator.visibleProperty()
                              .bind(service.runningProperty());
        this.progressIndicator.progressProperty()
                              .bind(service.progressProperty());
    }

    @Override
    public synchronized void onSetOntologyPathPredicate(final Property ontologyPathPredicate) {
        this.ontologyPathPredicate.setValue(ontologyPathPredicate);
    }

    @Override
    public void onAddNewDataNode(final DataNode<T> dataNode, final DataNodeRoot<T> dataNodeRoot) {
        LOGGER.trace("Adding new data node '{}' to root '{}'", dataNode.getName(), dataNodeRoot.getName());
        Platform.runLater(() -> {
            final TreeItem<DataNode<T>> parent = findTreeItem(dataNodeRoot, ontologyTreeView.getRoot());
            final TreeItem<DataNode<T>> child = new TreeItem<>(dataNode);
            parent.getChildren()
                  .add(child);
        });
    }

    @Override
    public void onAddMultipleDataNodes(final DataNode<T> dataNodesParent, final List<DataNode<T>> dataNodes, final DataNode<T> chosenDataNode) {
        LOGGER.trace("Adding multiple data nodes '{}' under '{}'", dataNodes, dataNodesParent);
        Platform.runLater(() -> {
            final TreeItem<DataNode<T>> treeItem = findTreeItem(dataNodesParent, ontologyTreeView.getRoot());
            treeItem.getChildren()
                    .addAll(dataNodes.stream()
                                     .map(TreeItem::new)
                                     .toList());
            treeItem.setExpanded(true);
        });
    }

    @Override
    public void onInvalidQuery(final String invalidQuery, final DBPediaEndpointTask.InitialSearchResult result) {
        Platform.runLater(() -> {
            // TODO: resource bundle
            // TODO: different alerts for different results
            assert result != DBPediaEndpointTask.InitialSearchResult.OK;
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            switch (result) {
                case SUBJECT_NOT_FOUND -> {
                    alert.setTitle("Invalid query");
                    alert.setHeaderText("ERROR - Invalid query");
                    final String wikiUrl = "https://en.wikipedia.org/wiki/";
                    final String queryWikiUrl = wikiUrl + invalidQuery;
                    final String exampleWikiUrl = wikiUrl + "Charles_IV,_Holy_Roman_Emperor";
                    final String exampleUri = "Charles IV, Holy Roman Emperor";
                    alert.setContentText(String.format(
                            "No results were found querying '%s'. The query must correspond to the wikipedia URL:%n%n%s%n%nYour query corresponds to an unknown URL:%n%n%s%n%nIn this example '%s' is" +
                            " a valid query. Spaces instead of underscores are allowed.",
                            invalidQuery,
                            exampleWikiUrl,
                            queryWikiUrl,
                            exampleUri));
                }
                case START_DATE_NOT_SELECTED -> {
                    alert.setTitle("Start date not chosen");
                    alert.setHeaderText("ERROR - Invalid start date");
                    alert.setContentText("Please choose a start date");
                }
                case END_DATE_NOT_SELECTED -> {
                    alert.setTitle("End date not chosen");
                    alert.setHeaderText("ERROR - Invalid end date");
                    alert.setContentText("Please choose a end date");
                }
                case PATH_NOT_SELECTED -> {
                    alert.setTitle("Path not chosen");
                    alert.setHeaderText("ERROR - Invalid path");
                    alert.setContentText("Please choose a path");
                }
                case UNKNOWN -> {
                    alert.setTitle("Unknown error");
                    alert.setHeaderText("ERROR - Unknown error occurred");
                    alert.setContentText(String.format("An unknown error happened with the query '%s'", invalidQuery));
                }
                default -> throw new UnsupportedOperationException("Result type not handled: " + result);
            }
            alert.show();
        });
    }

    @Override
    public void onSearchDone() {

    }

    @Override
    public void setStartAndDateProperty(final Property startDateProperty, final Property endDateProperty) {

    }

    /**
     * Callback for {@link SelectionModel#selectedItemProperty()} in the ontology list view. Displays the selected item in Wikipedia. The selected item must not be a root of any kind ({@link TreeItem}
     * root nor {@link DataNodeRoot}).
     *
     * @param observable the observable that was invalidated. not used, it's here just because of the signature of {@link InvalidationListener#invalidated(Observable)} method
     */
    private void onSelection(final Observable observable) {
        final TreeItem<DataNode<T>> selectedItem = ontologyTreeView.getSelectionModel()
                                                                   .getSelectedItem();
        final WebEngine engine = this.wikiPageWebView.getEngine();

        // a valid item is a non-root item that's not null
        final boolean hasSelectedValidItem = selectedItem != null && selectedItem != ontologyTreeView.getRoot() && !(selectedItem.getValue() instanceof DataNodeRoot<T>);
        if (!hasSelectedValidItem) {
            LOGGER.trace("Unloading web page because selected item was not valid. Cause: (null = {}, is root = {})", selectedItem == null, selectedItem != null);
            engine.load("");
            return;
        }

        final DataNode<T> dataNode = selectedItem.getValue();
        final String formattedItem = RDFNodeUtil.formatRDFNode(dataNode.getData());
        final String url = String.format(WIKI_URL, formattedItem);
        LOGGER.trace("Loading web page with URL {}", url);
        engine.load(url);
    }
}
