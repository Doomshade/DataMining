package cz.zcu.jsmahy.datamining.app.controller.cell;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;

import java.util.Map;

public class MetadataValueCellFactory extends TreeTableCell<Map.Entry<String, Object>, Object> {

    public MetadataValueCellFactory(final TreeTableColumn<Map.Entry<String, Object>, Object> x) {
    }

    @Override
    public void startEdit() {
        super.startEdit();
    }

    @Override
    public void commitEdit(final Object newValue) {
        super.commitEdit(newValue);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
    }

    @Override
    protected void updateItem(final Object item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            resetState();
            return;
        }

        final int index = getIndex();
        final TreeItem<Map.Entry<String, Object>> treeItem = getTreeTableView().getTreeItem(index);
        if (!treeItem.getChildren()
                     .isEmpty()) {
            resetState();
            return;
        }

        // the item can be:
        // Calendar (GregorianCalendar)
        // URL (RDFNode)
        // HashMap
        setText(item.toString());
        setTooltip(new Tooltip(item.toString()));
    }

    private void resetState() {
        setTooltip(null);
        setText(null);
        setGraphic(null);
    }
}
