package cz.zcu.jsmahy.datamining.app.controller.cell;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.*;
import java.util.*;
import java.util.function.Function;

public class MetadataValueCellFactory extends TreeTableCell<Map.Entry<String, Object>, Object> {
    private static final Logger LOGGER = LogManager.getLogger(MetadataValueCellFactory.class);
    private static final DateFormat DTF = new SimpleDateFormat("dd. MM. yyyy");
    // we need to preserve the ordering because we want to parse string as last
    private static final Map<Class<?>, Function<?, String>> OBJ_TO_STR_CONVERSIONS = new LinkedHashMap<>();
    // we need to validate the string format before converting it to the object
    // an invalid format returns null
    private static final Map<Class<?>, Function<String, ?>> STR_TO_OBJ_CONVERSIONS = new LinkedHashMap<>();

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

        // Resource
//        OBJ_TO_STR_CONVERSIONS.put(Resource.class, (Function<Resource, String>) Resource::getLocalName);
//        STR_TO_OBJ_CONVERSIONS.put(Resource.class, (Function<String, Resource>) str -> {
//            try {
//                new URI(str);
//            } catch (URISyntaxException e) {
//                return null;
//            }
//            try {
//                new URL(str);
//            } catch (MalformedURLException e) {
//                return null;
//            }
//            return ResourceFactory.createResource(str);
//        });

        // String
        OBJ_TO_STR_CONVERSIONS.put(String.class, (Function<String, String>) String::toString);
        STR_TO_OBJ_CONVERSIONS.put(String.class, (Function<String, String>) String::toString);
    }

    private final Runnable onCommitEdit;
    private TextField textField;

    /**
     * @param onCommitEdit A runnable that's run once the edit is committed. This could be used to refresh the UI state.
     */
    public MetadataValueCellFactory(Runnable onCommitEdit) {
        this.onCommitEdit = onCommitEdit;
    }

    @Override
    public void startEdit() {
        super.startEdit();
        updateTextField();
    }

    @Override
    public void commitEdit(final Object newValue) {
        super.commitEdit(newValue);

        getTableRow().getItem()
                     .setValue(newValue);
        updateTextAndTooltip(newValue);
        onCommitEdit.run();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateTextAndTooltip(getItem());
    }

    private void updateTextField() {
        if (textField == null) {
            createTextField();
        }
        setTooltip(null);
        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
    }

    private void alertInvalidValue() {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid value");
        alert.setHeaderText("Could not convert the value");
        alert.setContentText("The value you provided could not be converted to the original type. Make sure you follow the pattern");
        alert.show();
    }

    private void createTextField() {
        textField = new TextField();
        textField.textProperty()
                 .bindBidirectional(textProperty());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                final Object originalValue = getItem();
                final Object newValue = mapStringToData(textField.getText());
                // now make sure we converted it to the right object
                // if we converted it to the wrong object (e.g. a string) the data is invalid
                // as long as the classes are equal we presume the conversion was done right as we can't
                // assume the equals methods were implemented for each object

                if (originalValue.getClass()
                                 .isInstance(newValue) || newValue.getClass()
                                                                  .isInstance(originalValue)) {
                    commitEdit(newValue);
                } else {
                    alertInvalidValue();
                    cancelEdit();
                }
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
        LOGGER.trace("Updating text and tooltip of {} to {}", item, customText);
        setTextAndTooltip(customText);
    }

    private void setTextAndTooltip(String text) {
        setGraphic(null);
        setText(text);
        setTooltip(new Tooltip(text));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    private void resetState() {
        setTooltip(null);
        setText(null);
        setTooltip(null);
        setGraphic(null);
    }
}
