package cz.zcu.jsmahy.datamining.api;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public abstract class DefaultResponseResolver<D> implements ResponseResolver<D> {
    protected final ArbitraryDataHolder result = new DefaultArbitraryDataHolder();
    private final Object lock = new Object();
    /**
     * If true then the reference has been set. This exists because the default value of reference is {@code null}, and when setting null the request handler has no way of telling whether we actually
     * set it because it would check for the default state -- and that being null. This is a workaround for that issue.
     */
    private final ReadOnlyBooleanWrapper responseProperty = new ReadOnlyBooleanWrapper(false);

    @Override
    public final ArbitraryDataHolder getResponse() throws IllegalStateException {
        if (!hasResponseReady()) {
            throw new IllegalStateException("Response not ready yet.");
        }
        synchronized (lock) {
            responseProperty.set(false);
            return result;
        }
    }

    public ReadOnlyBooleanProperty hasResponseReadyProperty() {
        return responseProperty.getReadOnlyProperty();
    }

    public boolean hasResponseReady() {
        return responseProperty.get();
    }

    public void markResponseReady() {
        responseProperty.set(true);
    }
}
