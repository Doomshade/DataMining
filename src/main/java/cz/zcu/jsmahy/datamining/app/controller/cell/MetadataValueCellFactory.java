package cz.zcu.jsmahy.datamining.app.controller.cell;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public class MetadataValueCellFactory extends TreeTableCell<Map.Entry<String, Object>, Object> {
    private static final Logger LOGGER = LogManager.getLogger(MetadataValueCellFactory.class);
    private static final DateFormat DTF = new SimpleDateFormat("dd. MM. yyyy");
    private TextField textField;

    public MetadataValueCellFactory(final TreeTableColumn<Map.Entry<String, Object>, Object> x) {
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (textField == null) {
            createTextField();
        }
        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();

    }

    @Override
    public void commitEdit(final Object newValue) {
        super.commitEdit(newValue);
        setGraphic(null);
        getTableRow().getItem()
                     .setValue(newValue);
        updateTextAndTooltip(newValue);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
        updateTextAndTooltip(getItem());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    private void createTextField() {
        textField = new TextField();
        textField.textProperty()
                 .bindBidirectional(textProperty());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commitEdit(mapStringToData(textField.getText()));
                event.consume();
            }
        });
    }

    private Object mapStringToData(final String string) {
        try {
            final GregorianCalendar cal = new GregorianCalendar();
            final Date date = DTF.parse(string);
            cal.setTimeInMillis(date.getTime());
            return cal;
        } catch (ParseException ignored) {
        }
//        if (item instanceof Resource resource) {
//            return resource.getLocalName();
//        }
//        if (item instanceof String string) {
//            return string;
//        }
//        if (item instanceof Number number) {
//            return number.toString();
//        }
        return string;
    }

    @Override
    protected void updateItem(final Object item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            resetState();
            return;
        }
        if (isEditing()) {
            if (textField != null) {
                textField.setText(getCustomText(item));
            }
            setText(null);
            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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

        LOGGER.trace("Custom item class: {}", item.getClass());
        updateTextAndTooltip(item);

    }

    private String getCustomText(final Object item) {
        if (item == null) {
            return "<no name>";
        }
        if (item instanceof Calendar calendar) {
            final DateFormat dtf = new SimpleDateFormat("dd. MM. yyyy");
            return dtf.format(calendar.getTime());
        }
        if (item instanceof Resource resource) {
            return resource.getLocalName();
        }
        if (item instanceof String string) {
            return string;
        }
        if (item instanceof Number number) {
            return number.toString();
        }
        LOGGER.error("Unknown value class found: {}", item.getClass());
        return item.toString();
    }

    private void updateTextAndTooltip(final Object item) {
        final String customText = getCustomText(item);
        LOGGER.debug("Updating text and tooltip of {} to {}", item, customText);
        setTextAndTooltip(customText);
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
