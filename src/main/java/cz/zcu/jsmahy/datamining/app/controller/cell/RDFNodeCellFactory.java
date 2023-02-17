package cz.zcu.jsmahy.datamining.app.controller.cell;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.RequestProgressListener;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointAgent;
import cz.zcu.jsmahy.datamining.app.controller.MainController;
import cz.zcu.jsmahy.datamining.util.DialogHelper;
import cz.zcu.jsmahy.datamining.util.RDFNodeUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;
import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_URI;

/**
 * <p>Factory for {@link RDFNode} nodes in a {@link ListView}</p>
 * <p>Implementation details from the official site:
 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html">here</a></p>
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class RDFNodeCellFactory extends TreeCell<DataNode> {
    private static final Logger LOGGER = LogManager.getLogger(RDFNodeCellFactory.class);
    private TextField textField;

    public RDFNodeCellFactory(final ResourceBundle resources,
                              final MainController mainController,
                              final DialogHelper dialogHelper,
                              final DataNodeFactory nodeFactory,
                              final SparqlEndpointAgent<?> requestHandler,
                              final RequestProgressListener progressListener,
                              final Supplier<Collection<DataNode>> selectedItemSupplier) {
        emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                setContextMenu(null);
                return;
            }

            final ContextMenu contextMenu = new ContextMenu();

            final MenuItem searchItem = buildSearchItem(resources, dialogHelper, requestHandler, mainController);
            final MenuItem addRestrictionItem = buildAddRestrictionItem(resources);
//            final MenuItem addItem = buildAddItem(resources);
            final MenuItem editItem = buildEditItem(resources);
            final MenuItem deleteItem = buildDeleteItem(resources, progressListener, selectedItemSupplier);
            final MenuItem newLineItem = buildNewLineItem(resources, nodeFactory, requestHandler, mainController, progressListener);
            final MenuItem continueLineItem = buildContinueLineItem(resources);
            final ObservableList<MenuItem> items = contextMenu.getItems();
            if (getItem().isRoot()) {
                items.addAll(searchItem, addRestrictionItem, editItem, deleteItem);
            } else {
                items.addAll(newLineItem, continueLineItem, deleteItem);
            }
            setContextMenu(contextMenu);
        });
        final StringBinding stringBinding = Bindings.createStringBinding(() -> {
            return getItem() == null ? "" : getItem().getValue(METADATA_KEY_NAME, "<no name>");
        }, itemProperty());
        textProperty().bind(stringBinding);
    }

    private static Alert createPromptAlert(final Node content, final ResourceBundle resourceBundle) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes()
             .clear();
        alert.setTitle(resourceBundle.getString("confirm-deletion-dialog-title"));
        alert.setHeaderText(resourceBundle.getString("confirm-deletion-dialog-header"));
        alert.getButtonTypes()
             .addAll(ButtonType.YES, ButtonType.NO);
        alert.getDialogPane()
             .setContent(content);
        return alert;
    }

    private MenuItem buildContinueLineItem(final ResourceBundle resources) {
        final MenuItem menuItem = new MenuItem();
        menuItem.textProperty()
                .bind(Bindings.format(resources.getString("continue-line"), textProperty()));
        menuItem.setOnAction(event -> {

        });
        menuItem.setAccelerator(KeyCombination.keyCombination("CTRL + T"));
        return menuItem;
    }

    private MenuItem buildNewLineItem(final ResourceBundle resources,
                                      final DataNodeFactory nodeFactory,
                                      final SparqlEndpointAgent<?> requestHandler,
                                      final MainController mainController,
                                      final RequestProgressListener progressListener) {
        final MenuItem menuItem = new MenuItem(resources.getString("create-new-line"));
        menuItem.setOnAction(event -> {
            final DataNode dataNode = getItem();
            final Optional<? extends DataNode> dataNodeRootOpt = dataNode.findRoot();
            assert dataNodeRootOpt.isPresent(); // the item should not be a root, thus the item's root should be present

            final String name = dataNodeRootOpt.get()
                                               .getValue(METADATA_KEY_NAME)
                                               .orElse("<no name>") + " - copy";
            final DataNode newDataNodeRoot = nodeFactory.newRoot(name);
            progressListener.onCreateNewRoot(newDataNodeRoot);

            final Optional<String> query = dataNode.getValue(METADATA_KEY_URI);
            if (query.isEmpty()) {
                LOGGER.error("Could not create new line. Reason: Selected item '{}' does not have URI stored under the key '{}'", dataNode, METADATA_KEY_URI);
                return;
            }

            final Service<?> service = requestHandler.createBackgroundService(query.get(), newDataNodeRoot);
            mainController.bindQueryService(service);
            service.restart();
        });
        return menuItem;
    }

    private MenuItem buildSearchItem(final ResourceBundle resources, final DialogHelper dialogHelper, final SparqlEndpointAgent<?> sparqlEndpointAgent, final MainController mainController) {
        final MenuItem menuItem = new MenuItem();
        if (getItem() != null && getItem().isRoot() && !getItem().getChildren()
                                                                 .isEmpty()) {
            menuItem.textProperty()
                    .bind(Bindings.format(resources.getString("continue-search"), textProperty()));
        } else {
            menuItem.setText(resources.getString("search"));
        }
        menuItem.setOnAction(event -> dialogHelper.textInputDialog(resources.getString("subject-to-search"), resources.getString("search-subject"), searchValue -> {
            getTreeItem().setExpanded(true);
            assert getItem().isRoot();
            final String query = searchValue.replaceAll(" ", "_");
            if (query.isBlank()) {
                // TODO: show an alert
                return;
            }
            final Service<?> service = sparqlEndpointAgent.createBackgroundService(query, getItem());
            mainController.bindQueryService(service);
            service.setOnFailed(e -> LOGGER.throwing(service.getException()));
            service.restart();
        }));
        menuItem.setAccelerator(KeyCombination.keyCombination("CTRL + H"));
        return menuItem;
    }

    private MenuItem buildEditItem(final ResourceBundle resources) {
        final MenuItem menuItem = new MenuItem(resources.getString("edit"));
        menuItem.setOnAction(event -> getTreeView().edit(getTreeView().getSelectionModel()
                                                                      .getSelectedItem()));
        menuItem.setAccelerator(KeyCombination.keyCombination("CTRL + E"));
        return menuItem;
    }

    private MenuItem buildAddRestrictionItem(final ResourceBundle resources) {
        final MenuItem menuItem = new MenuItem();
        menuItem.textProperty()
                .bind(Bindings.format(resources.getString("ontology-prompt-add-restriction"), textProperty()));

        menuItem.setAccelerator(KeyCombination.keyCombination("CTRL + R"));
        return menuItem;
    }

    private void createTextField() {
        textField = new TextField();
        textField.textProperty()
                 .bindBidirectional(textProperty());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                final DataNode root = getItem();
                if (root.isRoot()) {
                    root.addMetadata(METADATA_KEY_NAME, textField.getText());
                    commitEdit(root);
                    event.consume();
                }
            }
        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
//        if (!getItem().isRoot()) {
//            return;
//        }
//        if (textField == null) {
//            createTextField();
//        }
//        setGraphic(textField);
//        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//        textField.requestFocus();
    }

    @Override
    public void commitEdit(final DataNode newValue) {
        super.commitEdit(newValue);
//        setGraphic(null);
//        setText(prettyFormat(newValue));
//        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
//        setGraphic(null);
//        setText(prettyFormat(getItem()));
//        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    private MenuItem buildDeleteItem(final ResourceBundle resources, final RequestProgressListener progressListener, final Supplier<Collection<DataNode>> selectedItemsSupplier) {
        final MenuItem menuItem = new MenuItem();
        menuItem.textProperty()
                .bind(Bindings.format(resources.getString("ontology-prompt-delete"), textProperty()));
        menuItem.setOnAction(event -> {
            final ListView<DataNode> nodes = new ListView<>();
            final Collection<DataNode> selectedDataNodes = selectedItemsSupplier.get();
            nodes.getItems()
                 .addAll(selectedDataNodes);
            nodes.setCellFactory(x -> new ListCell<>() {
                @Override
                protected void updateItem(final DataNode item, final boolean empty) {
                    super.updateItem(item, empty);
                    setText(item == null || empty ? null : item.getValue(METADATA_KEY_NAME, "<no name>"));
                }
            });

            // create a dialogue and have the user confirm the deletion
            final Alert alert = createPromptAlert(nodes, resources);
            if (alert.showAndWait()
                     .orElse(ButtonType.NO) == ButtonType.NO) {
                return;
            }


            ////////////////////////////////////////////
            for (final DataNode selectedItem : selectedDataNodes) {
                selectedItem.getParent()
                            .getChildren()
                            .remove(selectedItem);
            }
            progressListener.onDeleteDataNodes(selectedDataNodes);
        });
        menuItem.setAccelerator(KeyCombination.keyCombination("delete"));
        return menuItem;
    }

    private MenuItem buildAddItem(final ResourceBundle resources) {
        final MenuItem menuItem = new MenuItem();

        menuItem.textProperty()
                .bind(Bindings.format(resources.getString("ontology-prompt-add"), textProperty()));
        menuItem.setOnAction(event -> {
//            final DataNode<T> node = getItem();
            // code to edit item...
        });
        menuItem.setAccelerator(KeyCombination.keyCombination("A"));
        return menuItem;
    }

    /**
     * <p>Formats the {@link RDFNode} for pretty output in the {@link ListView}.</p>
     * <p>{@link RDFNodeUtil#formatRDFNode(RDFNode)} preserves special characters such as "_" - this method gets rids of those</p>
     *
     * @param node the node to format. if null, {@code "null"} is returned.
     *
     * @see RDFNodeUtil#formatRDFNode(RDFNode)
     */
    private String prettyFormat(DataNode node) {
        if (node == null) {
            return "";
        }
        return node.getValue(METADATA_KEY_NAME, "<no name>");
    }

    @Override
    protected void updateItem(final DataNode item, final boolean empty) {
        super.updateItem(item, empty);
//        if (empty) {
//            setText(null);
//            setGraphic(null);
//        } else {
//            if (isEditing()) {
//                if (textField != null) {
//                    textField.setText(prettyFormat(item));
//                }
//                setText(null);
//                setGraphic(textField);
//                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//            } else {
//                setGraphic(null);
//                setText(prettyFormat(item));
//                setContentDisplay(ContentDisplay.TEXT_ONLY);
//            }
//        }
    }
}
