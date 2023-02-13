package cz.zcu.jsmahy.datamining.app.controller.cell;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class MetadataValueCellFactory extends TreeTableCell<Map.Entry<String, Object>, Object> {
    private static final Logger LOGGER = LogManager.getLogger(MetadataValueCellFactory.class);

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

        // don't show the item's value if it has children
        final int index = getIndex();
        final TreeItem<Map.Entry<String, Object>> treeItem = getTreeTableView().getTreeItem(index);
        if (!treeItem.getChildren()
                     .isEmpty()) {
            resetState();
            setText("...");
            return;
        }

        // the item can be:
        // Calendar (GregorianCalendar)
        // Resource (Apache Jena)
        // String
        LOGGER.trace("Item class: {}", item.getClass());
        if (item instanceof Calendar calendar) {
            final DateFormat dtf = new SimpleDateFormat("dd. MM. yyyy");
            setTextAndTooltip(dtf.format(calendar.getTime()));
        } else if (item instanceof Resource resource) {
            setTextAndTooltip(resource.getLocalName());
        } else if (item instanceof String string) {
            setTextAndTooltip(string);
        } else if (item instanceof Number number) {
            setTextAndTooltip(number.toString());
        } else {
            setTextAndTooltip(item.toString());
            LOGGER.error("Unknown value class found: {}", item.getClass());
        }

    }

    private void setTextAndTooltip(String text) {
        setText(text);
        setTooltip(new Tooltip(text));
    }

    private void resetState() {
        setTooltip(null);
        setText(null);
        setGraphic(null);
    }
}
