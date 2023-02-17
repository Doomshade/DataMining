package cz.zcu.jsmahy.datamining.app.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.app.controller.cell.MetadataValueCellFactory;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import cz.zcu.jsmahy.datamining.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.export.FialaBPModule;
import cz.zcu.jsmahy.datamining.util.DialogHelper;
import cz.zcu.jsmahy.datamining.util.RDFNodeUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
import java.util.*;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;

/**
 * <p>The controller for main UI where user builds the ontology.</p>
 * <p>This controller uses the DBPedia endpoint and Fiala's Bachelor project program as its export.</p>
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class MainController implements Initializable {
    public static final String FILE_NAME_FORMAT = "%s.%s";
    // this could be loaded as a service someday :)
    // we'll basically ask for a module implementation to provide
    // this is VERY not needed
    private static final Module[] MODULES = new Module[] {
            new DataMiningModule(), new DBPediaModule(), new FialaBPModule()
    };
    private static final String WIKI_URL = "https://wikipedia.org/wiki/%s";
    private static final Logger LOGGER = LogManager.getLogger(MainController.class);
    @FXML
    private HBox leftPane;
    @FXML
    private BorderPane rootPane;
    @FXML
    private TreeView<DataNode> ontologyTreeView;
    @FXML
    private WebView wikiPageWebView;
    @FXML
    private ProgressIndicator progressIndicator;
    private DataNodeFactory nodeFactory;
    private DialogHelper dialogHelper;
    private final EventHandler<ActionEvent> createNewLineAction = e -> {
        final ResourceBundle lang = ResourceBundle.getBundle("lang");
        dialogHelper.textInputDialog(lang.getString("create-new-line"), "Název linie", lineName -> {
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
        });

    };
    @FXML
    private TreeTableView<Map.Entry<String, Object>> metadataTableView;
    private SparqlEndpointAgent<Void> requestHandler;
    private Injector injector;

    private static ObservableValue<Object> valueColumnFactory(final TreeTableColumn.CellDataFeatures<Map.Entry<String, Object>, Object> features) {
        // need to map values better
        return new SimpleObjectProperty<>(features.getValue()
                                                  .getValue()
                                                  .getValue());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(final URL location, final ResourceBundle resources) {
        this.injector = Guice.createInjector(MODULES);
        this.nodeFactory = injector.getInstance(DataNodeFactory.class);
        this.requestHandler = injector.getInstance(SparqlEndpointAgent.class);
        this.dialogHelper = injector.getInstance(DialogHelper.class);

        // sets up menu bar
        final MenuBar menuBar = createMenuBar(resources);
        this.rootPane.setTop(menuBar);
        this.rootPane.setPadding(new Insets(10));
        this.rootPane.disableProperty()
                     .bind(progressIndicator.visibleProperty());

        // sets up the ontology tree view
        final TreeItem<DataNode> root = new TreeItem<>(null);
        this.ontologyTreeView.setRoot(root);
        final MultipleSelectionModel<TreeItem<DataNode>> selectionModel = this.ontologyTreeView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);

        selectionModel.selectedItemProperty()
                      .addListener(this::onSelection);

        this.ontologyTreeView.setShowRoot(false);
        this.ontologyTreeView.getSelectionModel()
                             .setSelectionMode(SelectionMode.MULTIPLE);
        this.ontologyTreeView.setEditable(false);
        this.ontologyTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                event.consume();
            }
        });
        final RequestProgressListener progressListener = injector.getInstance(RequestProgressListener.class);
        progressListener.treeRootProperty()
                        .set(ontologyTreeView.getRoot());
        this.ontologyTreeView.setCellFactory(lv -> new RDFNodeCellFactory(resources,
                                                                          this,
                                                                          dialogHelper,
                                                                          nodeFactory,
                                                                          requestHandler,
                                                                          progressListener,
                                                                          () -> ontologyTreeView.getSelectionModel()
                                                                                                .getSelectedItems()
                                                                                                .stream()
                                                                                                .map(TreeItem::getValue)
                                                                                                .toList()));


        final ContextMenu contextMenu = createContextMenu(resources);
        this.ontologyTreeView.setContextMenu(contextMenu);

        // sets up the metadata list view
        this.metadataTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        this.metadataTableView.setPrefWidth(400);
        this.metadataTableView.setEditable(true);
        final ObservableList<TreeTableColumn<Map.Entry<String, Object>, ?>> columns = metadataTableView.getColumns();
        final TreeTableColumn<Map.Entry<String, Object>, String> keyColumn = new TreeTableColumn<>(resources.getString("key"));
        keyColumn.setCellValueFactory(features -> new SimpleStringProperty(features.getValue()
                                                                                   .getValue()
                                                                                   .getKey()));
        final TreeTableColumn<Map.Entry<String, Object>, Object> valueColumn = new TreeTableColumn<>(resources.getString("value"));
        valueColumn.setCellValueFactory(MainController::valueColumnFactory);
        valueColumn.setCellFactory(x -> new MetadataValueCellFactory(() -> {
            this.ontologyTreeView.refresh();
            final MultipleSelectionModel<TreeItem<DataNode>> sm = this.ontologyTreeView.getSelectionModel();
            sm.clearAndSelect(sm.getSelectedIndex());
        }));

        columns.setAll(keyColumn, valueColumn);
        VBox.setVgrow(metadataTableView, Priority.ALWAYS);
        VBox.setVgrow(ontologyTreeView, Priority.ALWAYS);
        this.metadataTableView.setRoot(new TreeItem<>());
        this.metadataTableView.setShowRoot(false);
        this.ontologyTreeView.getSelectionModel()
                             .selectedItemProperty()
                             .addListener(new TreeViewAndMetadataBinder());
    }

    private ContextMenu createContextMenu(final ResourceBundle resources) {
        final MenuItem addNewLineItem = createAddNewLineMenuItem(resources);

        return new ContextMenu(addNewLineItem);
    }

    private MenuBar createMenuBar(final ResourceBundle resources) {
        final MenuBar menuBar = new MenuBar();
        final String lineMenuText = resources.getString("line");
        final Menu lineMenu = new Menu(lineMenuText);
        final MenuItem newLineMenuItem = new MenuItem();
        final String newLineMenuText = resources.getString("create-new-line");
        newLineMenuItem.setText(newLineMenuText);
        newLineMenuItem.setAccelerator(KeyCombination.keyCombination("CTRL + N"));
        newLineMenuItem.setOnAction(createNewLineAction);
        lineMenu.getItems()
                .addAll(newLineMenuItem);

        final Menu fileMenu = new Menu(resources.getString("file"));
        fileMenu.setMnemonicParsing(true);

        final MenuItem exportToFile = new MenuItem(resources.getString("export"));
        exportToFile.setOnAction(e -> {
            final ObservableList<TreeItem<DataNode>> dataNodeRoots = ontologyTreeView.getRoot()
                                                                                     .getChildren();
            if (dataNodeRoots.size() == 0) {
                Platform.runLater(() -> {
                    final Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(resources.getString("nothing-to-export-dialog-title"));
                    alert.setHeaderText(resources.getString("nothing-to-export-dialog-header"));
                    final StringExpression content = Bindings.format(resources.getString("nothing-to-export-dialog-content"),
                                                                     newLineMenuItem.getAccelerator()
                                                                                    .getName(),
                                                                     lineMenuText,
                                                                     newLineMenuText);
                    alert.contentTextProperty()
                         .bind(content);
                    alert.show();
                });
                return;
            }

            final ObservableList<Service<?>> services = FXCollections.observableArrayList();
            final ObservableList<Service<?>> removedServices = FXCollections.observableArrayList();
            final BooleanBinding runningProperty = Bindings.size(services)
                                                           .greaterThan(0);
            final DoubleBinding progressProperty = Bindings.size(removedServices)
                                                           .divide((double) dataNodeRoots.size());
            bindProperties(runningProperty, progressProperty);

            final DataNodeSerializer serializer = injector.getInstance(DataNodeSerializer.class);
            for (final TreeItem<DataNode> root : dataNodeRoots) {
                final DataNode dataNodeRoot = root.getValue();
                final OutputStream out;
                try {
                    final String fileName = dataNodeRoot.getValue(METADATA_KEY_NAME, "<no name>");
                    final String fileExtension = serializer.getFileExtension();
                    //noinspection resource
                    out = new FileOutputStream(String.format(FILE_NAME_FORMAT, fileName, fileExtension));
                } catch (FileNotFoundException ex) {
                    throw new UncheckedIOException(ex);
                }
                final Service<Void> dataNodeExporter = new Service<>() {
                    @Override
                    protected Task<Void> createTask() {
                        return serializer.createSerializerTask(out, dataNodeRoot);
                    }
                };
                dataNodeExporter.setOnRunning(ev -> services.add(dataNodeExporter));
                dataNodeExporter.setOnSucceeded(ev -> {
                    removedServices.add(dataNodeExporter);
                    services.remove(dataNodeExporter);
                });
                dataNodeExporter.setOnCancelled(ev -> {
                    removedServices.add(dataNodeExporter);
                    services.remove(dataNodeExporter);
                });
                dataNodeExporter.setOnFailed(ev -> {
                    LOGGER.throwing(dataNodeExporter.getException());
                    removedServices.add(dataNodeExporter);
                    services.remove(dataNodeExporter);
                });
                dataNodeExporter.start();
            }
        });
        exportToFile.setAccelerator(KeyCombination.keyCombination("ALT + E"));
        fileMenu.getItems()
                .addAll(exportToFile);

        final Menu helpMenu = new Menu(resources.getString("help"));
        final MenuItem usage = new MenuItem(resources.getString("tool-usage"));
        usage.setOnAction(e -> Platform.runLater(() -> {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(resources.getString("tool-usage"));
            alert.setHeaderText(resources.getString("tool-usage-header"));
            final StringExpression expr = Bindings.format(resources.getString("tool-usage-content"),
                                                          newLineMenuItem.getAccelerator()
                                                                         .getName(),
                                                          lineMenuText,
                                                          newLineMenuText,
                                                          resources.getString("search"),
                                                          KeyCombination.keyCombination("CTRL + H") // hard coded key combination, this could change. too bad!
                                                                        .getName());
            alert.contentTextProperty()
                 .bind(expr);

            final int maxLineLength = Arrays.stream(expr.get()
                                                        .split("\n"))
                                            .mapToInt(String::length)
                                            .max()
                                            .orElse(0);
            final double extraPadding = 5.55d;
            alert.getDialogPane()
                 .setPrefWidth(maxLineLength * extraPadding);
            alert.setResizable(true);
            alert.show();
        }));
        helpMenu.getItems()
                .addAll(usage);

        menuBar.getMenus()
               .addAll(lineMenu, fileMenu, helpMenu);
        return menuBar;
    }

    private MenuItem createAddNewLineMenuItem(final ResourceBundle resources) {
        final MenuItem menuItem = new MenuItem();
        menuItem.setText(resources.getString("create-new-line"));
        menuItem.setAccelerator(KeyCombination.keyCombination("CTRL + N"));
        menuItem.setOnAction(createNewLineAction);
        return menuItem;
    }

    public void bindQueryService(final Service<?> service) {
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
        final boolean hasSelectedValidItem = (selectedItem != null) && (selectedItem != ontologyTreeView.getRoot()) && !selectedItem.getValue()
                                                                                                                                    .isRoot();
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

    private class TreeViewAndMetadataBinder implements ChangeListener<TreeItem<DataNode>> {

        // values deeper than 5 will be hard to display regardless of the limitation
        // if anything, an internal error occurred if the depth is >= 5
        private static final int MAX_DEPTH = 5;

        /**
         * Populates the children with the entry set of the map recursively
         *
         * @param children the children
         * @param entrySet the entry set
         */
        private void addMapsRecursively(List<TreeItem<Map.Entry<String, Object>>> children, Set<Map.Entry<String, Object>> entrySet) {
            addMapsRecursively(children, entrySet, 0);
        }

        @SuppressWarnings("unchecked")
        private void addMapsRecursively(List<TreeItem<Map.Entry<String, Object>>> children, Set<Map.Entry<String, Object>> entrySet, final int depth) {
            if (depth < 0 || depth >= MAX_DEPTH) {
                return;
            }

            for (final Map.Entry<String, Object> entry : entrySet) {
                final Object entryValue = entry.getValue();
                final TreeItem<Map.Entry<String, Object>> treeItem = new TreeItem<>(entry);
                children.add(treeItem);

                // the entry is a Map<String, ?>, add the node to the newly created tree item
                if (entryValue instanceof Map<?, ?> map && !map.isEmpty() && map.keySet()
                                                                                .iterator()
                                                                                .next() instanceof String) {
                    LOGGER.trace("Found map value. Adding {} recursively to {}'s children", map, treeItem);
                    addMapsRecursively(treeItem.getChildren(), ((Map<String, Object>) map).entrySet(), depth + 1);
                }
                // the entry is a List<ArbitraryDataHolder>, add the nodes to the newly created item
                else if (entryValue instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof ArbitraryDataHolder) {
                    // TODO: reconsider whether this should be shown
                    LOGGER.trace("Found list value. Adding {} recursively to {}'s children", list, treeItem);

                    final Map<String, Object> hugeMap = new HashMap<>();
                    int index = 0;
                    for (ArbitraryDataHolder dataHolder : (List<ArbitraryDataHolder>) list) {
                        hugeMap.put(String.valueOf(index++), dataHolder.getMetadata());
                    }

                    addMapsRecursively(treeItem.getChildren(), hugeMap.entrySet(), depth + 1);
                }

                LOGGER.trace("Added {} to children {}", treeItem, children);
            }
        }

        @Override
        public void changed(final ObservableValue<? extends TreeItem<DataNode>> observable, final TreeItem<DataNode> oldValue, final TreeItem<DataNode> newValue) {
            final ObservableList<TreeItem<Map.Entry<String, Object>>> children = metadataTableView.getRoot()
                                                                                                  .getChildren();
            children.clear();
            if (newValue == null) {
                return;
            }

            final DataNode dataNode = newValue.getValue();
            if (dataNode != null) {
                final Set<Map.Entry<String, Object>> entrySet = dataNode.getMetadata()
                                                                        .entrySet();
                final TreeItem<Map.Entry<String, Object>> idRow = new TreeItem<>(new AbstractMap.SimpleEntry<>("id", dataNode.getId()));
                children.add(idRow);
                addMapsRecursively(children, entrySet);
            }
        }
    }
}
