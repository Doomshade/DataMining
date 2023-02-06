package cz.zcu.jsmahy.datamining.app.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import cz.zcu.jsmahy.datamining.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.export.FialaBPModule;
import cz.zcu.jsmahy.datamining.export.FialaBPSerializer;
import cz.zcu.jsmahy.datamining.util.DialogHelper;
import cz.zcu.jsmahy.datamining.util.RDFNodeUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * <p>The controller for main UI where user builds the ontology.</p>
 * <p>This controller uses the DBPedia endpoint and Fiala's Bachelor project program as its export.</p>
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class MainController implements Initializable {
    public static final String FILE_NAME_FORMAT = "%s.%s";
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
    private static MainController instance = null;
    //<editor-fold desc="Attributes for query building">
    //<editor-fold desc="UI related attributes">
    @FXML
    private BorderPane rootPane;
    @FXML
    private TreeView<DataNode> ontologyTreeView;
    @FXML
    private WebView wikiPageWebView;
    @FXML
    private ProgressIndicator progressIndicator;
    //</editor-fold>
    private DataNodeFactory nodeFactory;
    //</editor-fold>
    private DialogHelper dialogHelper;
    private final EventHandler<ActionEvent> createNewLineAction = e -> {
        final ResourceBundle lang = ResourceBundle.getBundle("lang");
        dialogHelper.textInputDialog(lang.getString("create-new-line"), lineName -> {
            if (lineName.isBlank()) {
                Platform.runLater(() -> {
                    final Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid line");
                    alert.setHeaderText("Empty string");
                    alert.setContentText("Please fill in a non-blank value.");
                    alert.show();
                });
                return;
            }
            final DataNode dataNode = nodeFactory.newRoot(lineName);
            ontologyTreeView.getRoot()
                            .getChildren()
                            .add(new TreeItem<>(dataNode));
        }, "Title");

    };
    private SparqlEndpointAgent<Void> requestHandler;

    public static MainController getInstance() {
        return instance;
    }


    @Override
    @SuppressWarnings("unchecked")
    public void initialize(final URL location, final ResourceBundle resources) {
        instance = this;
        final DataMiningModule dataMiningModule = new DataMiningModule();
        final DBPediaModule dbPediaModule = new DBPediaModule();
        final FialaBPModule fialaBPModule = new FialaBPModule();

        final Injector injector = Guice.createInjector(dataMiningModule, dbPediaModule, fialaBPModule);
        this.nodeFactory = injector.getInstance(DataNodeFactory.class);
        this.requestHandler = injector.getInstance(SparqlEndpointAgent.class);
        this.dialogHelper = injector.getInstance(DialogHelper.class);
        final TreeItem<DataNode> root = new TreeItem<>(null);
        this.ontologyTreeView.setRoot(root);

        final MenuBar menuBar = createMenuBar(resources);
        this.rootPane.setTop(menuBar);
        this.rootPane.setPadding(new Insets(10));
        this.rootPane.disableProperty()
                     .bind(progressIndicator.visibleProperty());

        final MultipleSelectionModel<TreeItem<DataNode>> selectionModel = this.ontologyTreeView.getSelectionModel();
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
        this.ontologyTreeView.setCellFactory(lv -> new RDFNodeCellFactory(lv, resources, this, dialogHelper, nodeFactory, requestHandler));


        final ContextMenu contextMenu = createContextMenu(resources);
        this.ontologyTreeView.setContextMenu(contextMenu);
        injector.getInstance(RequestProgressListener.class)
                .treeRootProperty()
                .set(ontologyTreeView.getRoot());
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
        exportToFile.setOnAction(e -> {

            // TODO: This could be parallel
            final ObservableList<TreeItem<DataNode>> dataNodeRoots = ontologyTreeView.getRoot()
                                                                                     .getChildren();
            if (dataNodeRoots.size() == 0) {
                return;
            }
            final ObservableList<Service<?>> services = FXCollections.observableArrayList();
            final ObservableList<Service<?>> removedServices = FXCollections.observableArrayList();
            final DoubleBinding progressProperty = Bindings.size(removedServices)
                                                           .divide((double) dataNodeRoots.size());
            final BooleanBinding runningProperty = Bindings.size(services)
                                                           .greaterThan(0);
            bindProperties(runningProperty, progressProperty);

            final FialaBPSerializer serializer = new FialaBPSerializer();
            for (final TreeItem<DataNode> root : dataNodeRoots) {
                final DataNode dataNodeRoot = root.getValue();
                final OutputStream out;
                try {
                    final String fileName = dataNodeRoot.getValue("name", "<no name>");
                    final String fileExtension = serializer.getFileExtension();
                    out = new FileOutputStream(String.format(FILE_NAME_FORMAT, fileName, fileExtension));
                } catch (FileNotFoundException ex) {
                    throw new UncheckedIOException(ex);
                }
                // TODO: add option for different exporters
                final Service<Void> dataNodeExporter = new Service<>() {
                    @Override
                    protected Task<Void> createTask() {
                        return serializer.createSerializerTask(out, dataNodeRoot);
                    }
                };
                dataNodeExporter.setOnRunning(ev -> {
                    services.add(dataNodeExporter);

                });
                dataNodeExporter.setOnSucceeded(ev -> {
                    removedServices.add(dataNodeExporter);
                    services.remove(dataNodeExporter);
                });
                dataNodeExporter.setOnCancelled(ev -> {
                    removedServices.add(dataNodeExporter);
                    services.remove(dataNodeExporter);
                });
                dataNodeExporter.setOnFailed(ev -> {
                    removedServices.add(dataNodeExporter);
                    services.remove(dataNodeExporter);
                });
                dataNodeExporter.start();
            }
        });
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
    public void search(final DataNode root, final String searchValue) {
        if (searchValue.isBlank()) {
            LOGGER.trace("Search field is blank, not searching for anything.");
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid search");
            alert.setContentText("Please enter some text to search.");
            return;
        }

        final Service<Void> query = createSearchService(root, searchValue);
        bindQueryService(query);
        query.restart();
    }

    @SuppressWarnings("ThrowableNotThrown")
    private Service<Void> createSearchService(final DataNode root, final String searchValue) {
        final Service<Void> query = requestHandler.createBackgroundService(searchValue, root);
        query.setOnSucceeded(x -> ontologyTreeView.getSelectionModel()
                                                  .selectFirst());
        query.setOnFailed(x -> query.getException()
                                    .printStackTrace());
        return query;
    }

    public synchronized void bindQueryService(final Service<?> service) {
        bindProperties(service.runningProperty(), service.progressProperty());
    }

    public synchronized void bindProperties(ObservableValue<? extends Boolean> runningProperty, ObservableValue<? extends Number> progressProperty) {
        this.rootPane.disableProperty()
                     .bind(runningProperty);
        this.progressIndicator.visibleProperty()
                              .bind(runningProperty);
        this.progressIndicator.progressProperty()
                              .bind(progressProperty);
    }

    /**
     * Callback for {@link SelectionModel#selectedItemProperty()} in the ontology list view. Displays the selected item in Wikipedia. The selected item must not be a root of any kind ({@link TreeItem}
     * root nor {@link DataNode#isRoot()}
     *
     * @param observable the observable that was invalidated. not used, it's here just because of the signature of {@link InvalidationListener#invalidated(Observable)} method
     */
    private void onSelection(final Observable observable) {
        final TreeItem<DataNode> selectedItem = ontologyTreeView.getSelectionModel()
                                                                .getSelectedItem();
        final WebEngine engine = this.wikiPageWebView.getEngine();

        // a valid item is a non-root item that's not null
        final boolean hasSelectedValidItem = selectedItem != null && selectedItem != ontologyTreeView.getRoot() && !(selectedItem.getValue()
                                                                                                                                 .isRoot());
        if (!hasSelectedValidItem) {
            LOGGER.trace("Unloading web page because selected item was not valid. Cause: (null = {}, is root = {})", selectedItem == null, selectedItem != null);
            engine.load("");
            return;
        }

        final DataNode dataNode = selectedItem.getValue();
        final RDFNode rdfNode = dataNode.getValueUnsafe("rdfNode");
        final String formattedItem = RDFNodeUtil.formatRDFNode(rdfNode);
        final String url = String.format(WIKI_URL, formattedItem);
        LOGGER.trace("Loading web page with URL {}", url);
        engine.load(url);
    }
}
