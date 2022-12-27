package cz.zcu.jsmahy.datamining.api;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DataNodeReference<V> extends AtomicReference<DataNode<V>> {
    private final BooleanProperty hasMultipleReferences = new SimpleBooleanProperty(false);
    private final ReadOnlyBooleanWrapper finished = new ReadOnlyBooleanWrapper(false);

    public boolean getHasMultipleReferences() {
        return hasMultipleReferences.get();
    }

    public BooleanProperty hasMultipleReferencesProperty() {
        return hasMultipleReferences;
    }

    public void setHasMultipleReferences(final boolean hasMultipleReferences) {
        this.hasMultipleReferences.set(hasMultipleReferences);
    }

    public void finish() {
        finished.set(true);
    }

    public boolean isFinished() {
        return finished.get();
    }

    public ReadOnlyBooleanProperty finishedProperty() {
        return finished.getReadOnlyProperty();
    }
}
