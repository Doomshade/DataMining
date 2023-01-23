package cz.zcu.jsmahy.datamining.app.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jfoenix.controls.JFXSpinner;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import cz.zcu.jsmahy.datamining.api.RequestProgressListener;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.util.DialogHelper;
import cz.zcu.jsmahy.datamining.util.RDFNodeUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
    //<editor-fold desc="UI related attributes">
    @FXML
    private BorderPane rootPane;
    @FXML
    private TreeView<DataNode<T>> ontologyTreeView;
    @FXML
    private WebView wikiPageWebView;
    @FXML
    private JFXSpinner progressIndicator;
    private DataNodeFactory<T> nodeFactory;
    private DialogHelper dialogHelper;
    //</editor-fold>

    //<editor-fold desc="Attributes for query building">
    private final ObjectProperty<Property> ontologyPathPredicate = new SimpleObjectProperty<>();
    //</editor-fold>


    private static MainController<?> instance = null;

    public static MainController<?> getInstance() {
        return instance;
    }

    private final EventHandler<ActionEvent> createNewLineAction = e -> {
        final ResourceBundle lang = ResourceBundle.getBundle("lang");
        dialogHelper.textInputDialog(lang.getString("create-new-line"), lineName -> {
            final DataNodeRoot<T> dataNode = nodeFactory.newRoot(lineName);
            ontologyTreeView.getRoot()
                            .getChildren()
                            .add(new TreeItem<>(dataNode));
        }, "Title");

    };
    private RequestHandler<T, Void> requestHandler;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        instance = this;
        final Injector injector = Guice.createInjector(new DBPediaModule());
        nodeFactory = injector.getInstance(DataNodeFactory.class);
        requestHandler = injector.getInstance(RequestHandler.class);
        dialogHelper = injector.getInstance(DialogHelper.class);

        final MenuBar menuBar = createMenuBar(resources);
        rootPane.setTop(menuBar);

        rootPane.setPadding(new Insets(10));
        rootPane.disableProperty()
                .bind(progressIndicator.visibleProperty());

        final MultipleSelectionModel<TreeItem<DataNode<T>>> selectionModel = ontologyTreeView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);

        // TODO: this will likely be a popup
        selectionModel.selectedItemProperty()
                      .addListener(this::onSelection);

        ontologyTreeView.setShowRoot(false);
        ontologyTreeView.getSelectionModel()
                        .setSelectionMode(SelectionMode.MULTIPLE);
        ontologyTreeView.setEditable(true);
        ontologyTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                event.consume();
            }
        });
        ontologyTreeView.setCellFactory(lv -> new RDFNodeCellFactory<>(lv, resources, this, dialogHelper));

        final TreeItem<DataNode<T>> root = new TreeItem<>(null);
        ontologyTreeView.setRoot(root);
        final ContextMenu contextMenu = createContextMenu(resources);
        ontologyTreeView.setContextMenu(contextMenu);
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
     * Callback for {@link SelectionModel#selectedItemProperty()} in the ontology list view. Displays the selected item in Wikipedia. The selected item must not be a root of any kind ({@link TreeItem}
     * root nor {@link DataNodeRoot}).
     *
     * @param observable the observable that was invalidated. not used, it's here just because of the signature of {@link InvalidationListener#invalidated(Observable)} method
     */
    private void onSelection(final Observable observable) {
        final TreeItem<DataNode<T>> selectedItem = ontologyTreeView.getSelectionModel()
                                                                   .getSelectedItem();
        if (selectedItem == null) {
            LOGGER.debug("Could not handle ontology click because selected item was null.");
            return;
        }
        if (selectedItem == ontologyTreeView.getRoot() || selectedItem.getValue() instanceof DataNodeRoot<T>) {
            return;
        }

        final DataNode<T> dataNode = selectedItem.getValue();
        final String formattedItem = RDFNodeUtil.formatRDFNode(dataNode.getData());
        wikiPageWebView.getEngine()
                       .load(String.format(WIKI_URL, formattedItem));

    }

    /**
     * Handler for mouse press on the search button.
     *
     * @param root
     * @param searchValue
     */
    public void search(final TreeItem<DataNode<T>> root, final String searchValue) {
        if (searchValue.isBlank()) {
            LOGGER.info("Search field is blank, not searching for anything.");
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid search");
            alert.setContentText("Please enter some text to search.");
            return;
        }

        final Service<Void> query = requestHandler.query(searchValue, root);
        query.setOnSucceeded(x -> {
            ontologyTreeView.getSelectionModel()
                            .selectFirst();

        });
        query.setOnFailed(x -> {
            query.getException()
                 .printStackTrace();
        });
        query.restart();

        this.rootPane.disableProperty()
                     .bind(query.runningProperty());
        this.progressIndicator.visibleProperty()
                              .bind(query.runningProperty());
        this.progressIndicator.progressProperty()
                              .bind(query.progressProperty());
    }

    @Override
    public void onSetOntologyPathPredicate(final Property ontologyPathPredicate) {
        this.ontologyPathPredicate.setValue(ontologyPathPredicate);
    }

    @Override
    public TreeItem<DataNode<T>> onCreateNewDataNode(final DataNode<T> dataNode, final TreeItem<DataNode<T>> treeRoot) {
        final ObservableList<TreeItem<DataNode<T>>> treeChildren = treeRoot.getChildren();
        final TreeItem<DataNode<T>> currTreeItem = new TreeItem<>(dataNode);
        Platform.runLater(() -> treeChildren.add(currTreeItem));
        return currTreeItem;
    }

    @Override
    public void onAddMultipleDataNodes(final TreeItem<DataNode<T>> treeItem, final List<DataNode<T>> dataNodes, final DataNode<T> chosenDataNode) {
        Platform.runLater(() -> {
            treeItem.getChildren()
                    .addAll(dataNodes.stream()
                                     .map(TreeItem::new)
                                     .toList());
            treeItem.setExpanded(true);
        });
    }

    @Override
    public void onInvalidQuery(final String invalidQuery) {

        // TODO: resource bundle
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid query");
        alert.setHeaderText("ERROR - Invalid query");
        final String wikiUrl = "https://en.wikipedia.org/wiki/";
        final String queryWikiUrl = wikiUrl + invalidQuery;
        final String exampleWikiUrl = wikiUrl + "Charles_IV,_Holy_Roman_Emperor";
        final String exampleUri = "Charles IV, Holy Roman Emperor";
        alert.setContentText(String.format(
                "No results were found querying '%s'. The query must correspond to the wikipedia URL:%n%n%s%n%nYour query corresponds to an unknown URL:%n%n%s%n%nIn this example '%s' is a " +
                "valid query. Spaces instead of underscores are allowed.",
                invalidQuery,
                exampleWikiUrl,
                queryWikiUrl,
                exampleUri));
        alert.show();
    }

    @Override
    public void onSearchDone() {

    }
}
