package cz.zcu.jsmahy.datamining.app.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import cz.zcu.jsmahy.datamining.api.DialogHelper;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import cz.zcu.jsmahy.datamining.query.AsyncRequestHandler;
import cz.zcu.jsmahy.datamining.query.SparqlRequest;
import cz.zcu.jsmahy.datamining.query.UserAssistedAmbiguousInputResolver;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since
 */
public class MainController<T extends RDFNode> implements Initializable {
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
    private static final String DBPEDIA_SERVICE = "http://dbpedia.org/sparql/";
    private static final int LINE_BREAK_LIMIT = 20;
    private static int SEQUENCE_NUM = 1;
    @FXML
    private JFXTextField searchField;
    @FXML
    private JFXButton searchButton;
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

    private final EventHandler<ActionEvent> createNewLineAction = e -> {
        final ResourceBundle lang = ResourceBundle.getBundle("lang");
        dialogHelper.textInputDialog(lang.getString("create-new-line"), lineName -> {
            final DataNodeRoot<T> dataNode = nodeFactory.newRoot(lineName);
            ontologyTreeView.getRoot()
                            .getChildren()
                            .add(new TreeItem<>(dataNode));
        }, "Title");

    };
    private AsyncRequestHandler<T, Void> requestHandler;

    private static synchronized void exit() {
        LOGGER.info("Exiting application...");
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        final Injector injector = Guice.createInjector(new DBPediaModule());
        nodeFactory = injector.getInstance(DataNodeFactory.class);
        requestHandler = injector.getInstance(AsyncRequestHandler.class);
        dialogHelper = injector.getInstance(DialogHelper.class);

        // when user has focus on search field and presses enter -> search
//        searchField.setOnKeyPressed(e -> {
//            if (e.getCode() == KeyCode.ENTER) {
//                search(ontologyTreeView.getRoot());
//            }
//        });
//        searchField.requestFocus();

        final MenuBar menuBar = createMenuBar(resources);
        rootPane.setTop(menuBar);

        rootPane.setPadding(new Insets(10));
        // this will ensure the pane is disabled when progress indicator is visible
        rootPane.disableProperty()
                .bind(progressIndicator.visibleProperty());

        final MultipleSelectionModel<TreeItem<DataNode<T>>> selectionModel = ontologyTreeView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);

        // show a web view on selection
        // TODO: this will likely be a popup
        selectionModel.selectedItemProperty()
                      .addListener(this::onSelection);

        // use custom cell factory for display
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
        exportToFile.setAccelerator(KeyCombination.keyCombination("CTRL + E"));

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
        final String formattedItem = RDFNodeCellFactory.formatRDFNode(dataNode.getData());
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
        final SparqlRequest<T, Void> request = SparqlRequest.<T, Void>builder()
                                                            .requestPage(searchValue)
                                                            .namespace("http://dbpedia.org/ontology/")
                                                            .link("doctoralAdvisor")
                                                            .treeRoot(root)
                                                            .ambiguousInputResolver(new UserAssistedAmbiguousInputResolver<>())
                                                            .build();
        final Service<Void> query = requestHandler.query(request);
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
}
