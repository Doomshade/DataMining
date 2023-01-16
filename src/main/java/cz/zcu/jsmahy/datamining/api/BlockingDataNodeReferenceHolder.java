package cz.zcu.jsmahy.datamining.api;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public class BlockingDataNodeReferenceHolder<V> extends DataNodeReferenceHolder<V> {
    /**
     * If true then the reference has been set. This exists because the default value of reference is {@code null}, and when setting null the request handler has no way of telling whether we actually
     * set it because it would check for the default state -- and that being null. This is a workaround for that issue.
     */
    private final ReadOnlyBooleanWrapper finished = new ReadOnlyBooleanWrapper(false);

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
