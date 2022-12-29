package cz.zcu.jsmahy.datamining.app.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.SparqlRequest;
import cz.zcu.jsmahy.datamining.query.UserAssistedAmbiguitySolver;
import javafx.application.Platform;
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
    private final EventHandler<ActionEvent> newLineAction = e -> {
        final DataNodeRoot<T> dataNode = nodeFactory.newRoot("Linie # " + SEQUENCE_NUM++);
        ontologyTreeView.getRoot()
                        .getChildren()
                        .add(new TreeItem<>(dataNode));
    };


    private static synchronized void exit() {
        LOGGER.info("Exiting application...");
        Platform.exit();
        System.exit(0);
    }

    private RequestHandler<T, Void> requestHandler;

    private static int SEQUENCE_NUM = 1;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        final Injector injector = Guice.createInjector(new DBPediaModule());
        nodeFactory = injector.getInstance(DataNodeFactory.class);
        requestHandler = injector.getInstance(RequestHandler.class);

        // when user has focus on search field and presses enter -> search
//        searchField.setOnKeyPressed(e -> {
//            if (e.getCode() == KeyCode.ENTER) {
//                search(ontologyTreeView.getRoot());
//            }
//        });
//        searchField.requestFocus();

        final MenuBar menuBar = new MenuBar();

        final Menu fileMenu = new Menu("_" + resources.getString("file"));
        fileMenu.setMnemonicParsing(true);

        final MenuItem exportToFile = new MenuItem(resources.getString("export"));
        exportToFile.setAccelerator(KeyCombination.keyCombination("CTRL + E"));
        final MenuItem newLine = new MenuItem(resources.getString("create-new-line"));
        newLine.setAccelerator(KeyCombination.keyCombination("CTRL + N"));
        newLine.setOnAction(newLineAction);
        fileMenu.getItems()
                .addAll(exportToFile, newLine);

        menuBar.getMenus()
               .add(fileMenu);
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
        ontologyTreeView.setCellFactory(lv -> new RDFNodeCellFactory<>(lv, resources, this));

        final TreeItem<DataNode<T>> root = new TreeItem<>(null);
        ontologyTreeView.setRoot(root);
        final MenuItem addNewLineItem = buildAddNewLineItem(resources);

        final ContextMenu contextMenu = new ContextMenu(addNewLineItem);
        ontologyTreeView.setContextMenu(contextMenu);
    }

    private MenuItem buildAddNewLineItem(final ResourceBundle resources) {
        final MenuItem menuItem = new MenuItem();
        menuItem.setText("Vytvořit novou linii");
        menuItem.setOnAction(newLineAction);
        return menuItem;
    }

    /**
     * Callback for {@link SelectionModel#selectedItemProperty()} in the ontology list view.
     *
     * @param observable the observable that was invalidated
     */
    private void onSelection(final Observable observable) {
        final TreeItem<DataNode<T>> selectedItem = ontologyTreeView.getSelectionModel()
                                                                   .getSelectedItem();
        if (selectedItem == null) {
            LOGGER.info("Could not handle ontology click because selected item was null.");
            return;
        }
        if (selectedItem == ontologyTreeView.getRoot()) {
            return;
        }
        if (selectedItem.getValue() instanceof DataNodeRoot<T>) {
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
            final Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Invalid search");
            alert.setContentText("Please enter some text to search.");
            return;
        }
        final SparqlRequest<T, Void> request = SparqlRequest.<T, Void>builder()
                                                            .requestPage(searchValue)
                                                            .namespace("http://dbpedia.org/ontology/")
                                                            .link("parent")
                                                            .treeRoot(root)
                                                            .ambiguitySolver(new UserAssistedAmbiguitySolver<>())
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
