package cz.zcu.jsmahy.datamining.app.controller.cell;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import cz.zcu.jsmahy.datamining.util.DialogHelper;
import cz.zcu.jsmahy.datamining.app.controller.MainController;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * <p>Factory for {@link RDFNode} nodes in a {@link ListView}</p>
 * <p>Implementation details from the official site:
 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html">here</a></p>
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class RDFNodeCellFactory<T extends RDFNode> extends TreeCell<DataNode<T>> {
    public static final String SPECIAL_CHARACTERS = "_";
    private static final Logger LOGGER = LogManager.getLogger(RDFNodeCellFactory.class);
    private final TreeView<DataNode<T>> treeView;
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final MainController<T> mainController;
    private TextField textField;

    public RDFNodeCellFactory(final TreeView<DataNode<T>> treeView, final ResourceBundle resources, final MainController<T> mainController, final DialogHelper dialogHelper) {
        this.treeView = treeView;
        this.mainController = mainController;
        emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if (!isNowEmpty) {
                // TODO: context menu for "add/continue line"
                final ContextMenu contextMenu = new ContextMenu();

                final MenuItem searchItem = buildSearchItem(resources, dialogHelper);
                final MenuItem addRestrictionItem = buildAddRestrictionItem(resources);
                final MenuItem addItem = buildAddItem(resources);
                final MenuItem editItem = buildEditItem(resources);
                final MenuItem deleteItem = buildDeleteItem(resources);
                final ObservableList<MenuItem> items = contextMenu.getItems();
                if (getItem() instanceof DataNodeRoot<T>) {
                    items.addAll(searchItem, addRestrictionItem, addItem, editItem, deleteItem);
                } else {
                    items.addAll(addItem, deleteItem);
                }
                setContextMenu(contextMenu);
            } else {
                setContextMenu(null);
            }
        });
    }

    private MenuItem buildSearchItem(final ResourceBundle resources, final DialogHelper dialogHelper) {
        final MenuItem menuItem = new MenuItem(resources.getString("search"));
        menuItem.setOnAction(event -> {
            dialogHelper.textInputDialog(resources.getString("item-to-search"), searchValue -> {
                getTreeItem().setExpanded(true);
                mainController.search(getTreeItem(), searchValue.replaceAll(" ", "_"));
            }, "Title");
        });
        menuItem.setAccelerator(KeyCombination.keyCombination("CTRL + H"));
        return menuItem;
    }

    /**
     * <p>Formats the {@link RDFNode} to be "pretty" on output.</p>
     * <p>This method will strip any domain off the {@link RDFNode} if it's a {@link Resource}. If it's a {@link Literal}, this will
     * simply return {@link Literal#toString()}. If it's neither, it will return {@link RDFNode#toString()}.</p>
     *
     * @param node the node to format. if null, {@code "null"} is returned.
     *
     * @return {@link RDFNode} in a simple {@link String} representation
     */
    public static <T extends RDFNode> String formatRDFNode(T node) {
        if (node == null) {
            return "null";
        }
        final Marker marker = MarkerManager.getMarker("node-type");
        if (node.isLiteral()) {
            String str = node.asLiteral()
                             .toString();
            final int languageIndex = str.lastIndexOf('@');
            if (languageIndex > 0) {
                str = str.substring(0, languageIndex);
            }
            LOGGER.trace(marker, "Literal \"{}\"", str);
            return str;
        }
        if (node.isResource()) {
            final Resource resource = node.asResource();
            final String uri = resource.getURI();
            final int lastPartIndex = uri.lastIndexOf('/') + 1;

            final String localName = uri.substring(lastPartIndex);
            LOGGER.trace(marker, "Resource \"{}\"", localName);
            return localName;
        }

        LOGGER.debug(marker, "RDFNode \"{}\" was neither literal or resource, using default toString method.", node);
        return node.toString();
    }

    private MenuItem buildEditItem(final ResourceBundle resources) {
        final MenuItem menuItem = new MenuItem(resources.getString("edit"));
        menuItem.setOnAction(event -> {
            getTreeView().edit(getTreeView().getSelectionModel()
                                            .getSelectedItem());
        });
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
                if (getItem() instanceof DataNodeRoot<T> item) {
                    item.setName(textField.getText());
                    commitEdit(item);
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

    private MenuItem buildDeleteItem(final ResourceBundle resources) {
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
            final DataNode<T> node = getItem();
            // code to edit item...
        });
        menuItem.setAccelerator(KeyCombination.keyCombination("A"));
        return menuItem;
    }

    /**
     * <p>Formats the {@link RDFNode} for pretty output in the {@link ListView}.</p>
     * <p>{@link RDFNodeCellFactory#formatRDFNode(RDFNode)} preserves special characters such as "_" - this method gets rids of those</p>
     *
     * @param node the node to format. if null, {@code "null"} is returned.
     *
     * @see RDFNodeCellFactory#formatRDFNode(RDFNode)
     */
    private String prettyFormat(DataNode<T> node) {
        if (node == null) {
            return "";
        }
        return node instanceof DataNodeRoot<T> ? ((DataNodeRoot<T>) node).getName() : formatRDFNode(node.getData()).replaceAll(SPECIAL_CHARACTERS, " ");
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
