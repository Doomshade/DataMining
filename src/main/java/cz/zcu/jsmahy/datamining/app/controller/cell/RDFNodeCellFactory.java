package cz.zcu.jsmahy.datamining.app.controller.cell;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointAgent;
import cz.zcu.jsmahy.datamining.app.controller.MainController;
import cz.zcu.jsmahy.datamining.util.DialogHelper;
import cz.zcu.jsmahy.datamining.util.RDFNodeUtil;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * <p>Factory for {@link RDFNode} nodes in a {@link ListView}</p>
 * <p>Implementation details from the official site:
 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html">here</a></p>
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class RDFNodeCellFactory<T extends RDFNode> extends TreeCell<DataNode<T>> {
    private TextField textField;

    public RDFNodeCellFactory(final TreeView<DataNode<T>> treeView,
                              final ResourceBundle resources,
                              final MainController<T> mainController,
                              final DialogHelper dialogHelper,
                              final DataNodeFactory<T> nodeFactory,
                              final SparqlEndpointAgent<T, ?> requestHandler) {
        emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                setContextMenu(null);
                return;
            }

            // TODO: context menu for "add/continue line"
            final ContextMenu contextMenu = new ContextMenu();

            final MenuItem searchItem = buildSearchItem(resources, dialogHelper, requestHandler, mainController);
            final MenuItem addRestrictionItem = buildAddRestrictionItem(resources);
            final MenuItem addItem = buildAddItem(resources);
            final MenuItem editItem = buildEditItem(resources);
            final MenuItem deleteItem = buildDeleteItem(resources, treeView);
            final MenuItem newLineItem = buildNewLineItem(resources, nodeFactory, requestHandler, treeView, mainController);
            final MenuItem continueLineItem = buildContinueLineItem(resources);
            final ObservableList<MenuItem> items = contextMenu.getItems();
            if (getItem() instanceof DataNodeRoot<T>) {
                items.addAll(searchItem, addRestrictionItem, addItem, editItem, deleteItem);
            } else {
                items.addAll(newLineItem, continueLineItem, addItem, deleteItem);
            }
            setContextMenu(contextMenu);
        });
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
                                      final DataNodeFactory<T> nodeFactory,
                                      final SparqlEndpointAgent<T, ?> requestHandler,
                                      final TreeView<DataNode<T>> treeView,
                                      final MainController<T> mainController) {
        final MenuItem menuItem = new MenuItem(resources.getString("create-new-line"));
        menuItem.setOnAction(event -> {
            final TreeItem<DataNode<T>> root = treeView.getRoot();
            final Optional<DataNodeRoot<T>> dataNodeRootOpt = getItem().findRoot();
            assert dataNodeRootOpt.isPresent(); // the item should not be a root, thus the item's root should be present
            final DataNodeRoot<T> newDataNodeRoot = nodeFactory.newRoot(dataNodeRootOpt.get()
                                                                                       .getName() + " - copy");
            root.getChildren()
                .add(new TreeItem<>(newDataNodeRoot));
            final Service<?> service = requestHandler.createBackgroundService(getItem().getUri(), newDataNodeRoot);
            mainController.bindService(service);
            service.restart();
        });
        return menuItem;
    }

    private MenuItem buildSearchItem(final ResourceBundle resources, final DialogHelper dialogHelper, final SparqlEndpointAgent<T, ?> sparqlEndpointAgent, final MainController<T> mainController) {
        final MenuItem menuItem = new MenuItem(resources.getString("search"));
        menuItem.setOnAction(event -> dialogHelper.textInputDialog(resources.getString("item-to-search"), searchValue -> {
            getTreeItem().setExpanded(true);
            final Service<?> service = sparqlEndpointAgent.createBackgroundService(searchValue.replaceAll(" ", "_"), (DataNodeRoot<T>) getItem());
            mainController.bindService(service);
            service.restart();
        }, "Title"));
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
                if (getItem() instanceof DataNodeRoot<T> root) {
                    root.setName(textField.getText());
                    commitEdit(root);
                    event.consume();
                }
            }
        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (!(getItem() instanceof DataNodeRoot<T>)) {
            return;
        }
        if (textField == null) {
            createTextField();
        }
        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
    }

    @Override
    public void commitEdit(final DataNode<T> newValue) {
        super.commitEdit(newValue);
        setGraphic(null);
        setText(prettyFormat(newValue));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
        setText(prettyFormat(getItem()));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    private MenuItem buildDeleteItem(final ResourceBundle resources, final TreeView<DataNode<T>> treeView) {
        final MenuItem menuItem = new MenuItem();
        menuItem.textProperty()
                .bind(Bindings.format(resources.getString("ontology-prompt-delete"), textProperty()));
        menuItem.setOnAction(event -> {
            final ObservableList<TreeItem<DataNode<T>>> selectedItems = treeView.getSelectionModel()
                                                                                .getSelectedItems();
            // create a copy because we modify the inner list which would throw an exception otherwise
            final List<TreeItem<DataNode<T>>> temp = new ArrayList<>(selectedItems);
            for (final TreeItem<DataNode<T>> selectedItem : temp) {
                selectedItem.getParent()
                            .getChildren()
                            .remove(selectedItem);
            }
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
    private String prettyFormat(DataNode<T> node) {
        if (node == null) {
            return "";
        }
        return node.getName();
    }

    @Override
    protected void updateItem(final DataNode<T> item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(prettyFormat(item));
                }
                setText(null);
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setGraphic(null);
                setText(prettyFormat(item));
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }
}
