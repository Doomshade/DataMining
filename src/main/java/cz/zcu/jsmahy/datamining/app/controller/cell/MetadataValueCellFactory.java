package cz.zcu.jsmahy.datamining.app.controller.cell;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.*;
import java.util.*;
import java.util.function.Function;

public class MetadataValueCellFactory extends TreeTableCell<Map.Entry<String, Object>, Object> {
    private static final Logger LOGGER = LogManager.getLogger(MetadataValueCellFactory.class);
    private static final DateFormat DTF = new SimpleDateFormat("dd. MM. yyyy");
    private static final Map<Class<?>, Function<?, String>> OBJ_TO_STR_CONVERSIONS = new HashMap<>();
    // we need to validate the string format before converting it to the object
    // an invalid format returns null
    private static final Map<Class<?>, Function<String, ?>> STR_TO_OBJ_CONVERSIONS = new HashMap<>();

    static {
        // Calendar
        OBJ_TO_STR_CONVERSIONS.put(Calendar.class, (Function<Calendar, String>) calendar -> DTF.format(calendar.getTime()));
        STR_TO_OBJ_CONVERSIONS.put(Calendar.class, (Function<String, Calendar>) str -> {
            final GregorianCalendar cal = new GregorianCalendar();
            final Date date;
            try {
                date = DTF.parse(str);
            } catch (ParseException e) {
                return null;
            }
            cal.setTimeInMillis(date.getTime());
            return cal;
        });

        // Resource
        OBJ_TO_STR_CONVERSIONS.put(Resource.class, (Function<Resource, String>) Resource::getLocalName);
        STR_TO_OBJ_CONVERSIONS.put(Resource.class, (Function<String, Resource>) str -> {
            try {
                new URI(str);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return ResourceFactory.createResource(str);
        });

        // String
        OBJ_TO_STR_CONVERSIONS.put(String.class, (Function<String, String>) String::toString);
        STR_TO_OBJ_CONVERSIONS.put(String.class, (Function<String, String>) String::toString);

        // Number
        OBJ_TO_STR_CONVERSIONS.put(Number.class, (Function<Number, String>) Number::toString);
        STR_TO_OBJ_CONVERSIONS.put(Number.class, (Function<String, Number>) str -> {
            try {
                return NumberFormat.getInstance()
                                   .parse(str);
            } catch (ParseException e) {
                return null;
            }
        });
    }

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
        for (final Map.Entry<Class<?>, Function<String, ?>> entry : STR_TO_OBJ_CONVERSIONS.entrySet()) {
            final Object convertedObj = entry.getValue()
                                             .apply(string);
            if (convertedObj != null) {
                return convertedObj;
            }
        }
        throw new IllegalArgumentException(MessageFormat.format("Failed to convert {0} to string format", string));
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
        final Class<?> objClass = item.getClass();
        for (final Map.Entry<Class<?>, Function<?, String>> entry : OBJ_TO_STR_CONVERSIONS.entrySet()) {
            final Class<?> key = entry.getKey();
            if (key.isAssignableFrom(objClass)) {
                final Function<Object, String> value = (Function<Object, String>) entry.getValue();
                return value.apply(item);
            }
        }
        LOGGER.error("Unknown value class found: {}", objClass);
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
