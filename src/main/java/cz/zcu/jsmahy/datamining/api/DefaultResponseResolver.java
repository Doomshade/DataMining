package cz.zcu.jsmahy.datamining.api;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

/**
 * Default implementation of the {@link ResponseResolver}. It's advised to implement this class if you want to create a new {@link ResponseResolver}, although not mandatory.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public abstract class DefaultResponseResolver<D> implements ResponseResolver<D> {
    protected final ArbitraryDataHolder result = new DefaultArbitraryDataHolder();
    private final Object lock = new Object();
    /**
     * If true then the reference has been set. This exists because the default value of reference is {@code null}, and when we set the reference to {@code null} the request handler has no way of
     * telling whether the reference was actually set.
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

    @Override
    public ReadOnlyBooleanProperty hasResponseReadyProperty() {
        return responseProperty.getReadOnlyProperty();
    }

    @Override
    public boolean hasResponseReady() {
        return responseProperty.get();
    }

    @Override
    public void markResponseReady() {
        responseProperty.set(true);
    }
}
