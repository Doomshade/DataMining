package cz.zcu.jsmahy.datamining.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ChoiceBoxTreeTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Function;

public class IBTemplateController implements Initializable {
    @FXML
    private VBox vbox;
    @FXML
    private JFXTreeTableView<IBTemplateTreeObject> tree;

    @FXML
    private JFXButton treeTableViewRemove;

    @FXML
    private JFXButton treeTableViewAdd;

    @FXML
    private JFXTreeTableColumn<IBTemplateTreeObject, String> valueCol;

    @FXML
    private JFXTreeTableColumn<IBTemplateTreeObject, Boolean> requiredCol;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        vbox.setPadding(new Insets(35, 25, 35, 25));
        setupCellValueFactory(valueCol, IBTemplateTreeObject::valueProperty);
        setupCellValueFactory(requiredCol, IBTemplateTreeObject::requiredProperty);
        setupTree();
        setupColumns();
    }

    private void setupTree() {
        ObservableList<IBTemplateTreeObject> list = FXCollections.observableArrayList();
        tree.setRoot(new RecursiveTreeItem<>(list, RecursiveTreeObject::getChildren));
        tree.setShowRoot(false);
        tree.setEditable(true);

        treeTableViewAdd.setOnMouseClicked((e) -> {
            list.add(new IBTemplateTreeObject("value", true));
        });
        treeTableViewRemove.setOnMouseClicked((e) -> {
            list.remove(tree.getSelectionModel()
                            .selectedItemProperty()
                            .get()
                            .getValue());
        });

    }

    private void setupColumns() {
        valueCol.setOnEditCommit((TreeTableColumn.CellEditEvent<IBTemplateTreeObject, String> t) -> {
            final TreeTableView<IBTemplateTreeObject> treeView = t.getTreeTableView();
            treeView.getTreeItem(t.getTreeTablePosition()
                                  .getRow())
                    .getValue().value.set(t.getNewValue());
        });
        valueCol.setCellFactory((TreeTableColumn<IBTemplateTreeObject, String> param) -> new GenericEditableTreeTableCell<>(new TextFieldEditorBuilder()));

        requiredCol.setOnEditCommit((TreeTableColumn.CellEditEvent<IBTemplateTreeObject, Boolean> t) -> {
            t.getTreeTableView()
             .getTreeItem(t.getTreeTablePosition()
                           .getRow())
             .getValue().required.set(t.getNewValue());
        });
        requiredCol.setCellFactory((TreeTableColumn<IBTemplateTreeObject, Boolean> param) -> new ChoiceBoxTreeTableCell<>(true, false));
    }

    private <T> void setupCellValueFactory(JFXTreeTableColumn<IBTemplateTreeObject, T> column, Function<IBTemplateTreeObject, ObservableValue<T>> mapper) {
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<IBTemplateTreeObject, T> param) -> {
            if (column.validateValue(param)) {
                return mapper.apply(param.getValue()
                                         .getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
    }

    public void submit(MouseEvent mouseEvent) {
        System.out.println("ADDING");
    }

    private static final class IBTemplateTreeObject extends RecursiveTreeObject<IBTemplateTreeObject> {
        private final StringProperty value;
        private final BooleanProperty required;

        public IBTemplateTreeObject(String value, boolean required) {
            this.value = new SimpleStringProperty(value);
            this.required = new SimpleBooleanProperty(required);
        }

        public StringProperty valueProperty() {
            return value;
        }

        public BooleanProperty requiredProperty() {
            return required;
        }
    }
}
