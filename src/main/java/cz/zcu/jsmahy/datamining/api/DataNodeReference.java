package cz.zcu.jsmahy.datamining.api;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link DataNode} reference with convenience methods to distinguish various outputs. The main reason being the output being {@code null} because the user chose nothing -- that doesn't necessarily
 * mean he hasn't chosen ANYTHING yet. The finished property helps distinguish that.
 * TODO: make it a reference to a collection of data nodes
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DataNodeReference<V> extends AtomicReference<DataNode<V>> {
    private final BooleanProperty hasMultipleReferences = new SimpleBooleanProperty(false);
    /**
     * If true then the reference has been set. This exists because the default value of reference is {@code null}, and when setting null the request handler has no way of telling whether we actually
     * set it because it would check for the default state -- and that being null. This is a workaround for that issue.
     */
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

    /**
     * Marks this reference as finished -- the reference has been set.
     */
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
