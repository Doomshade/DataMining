package cz.zcu.jsmahy.datamining.api;

import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * The default implementation of {@link ArbitraryDataHolder}. This implementation is thread-safe, i.e. a synchronized {@link Map} is used under the hood.
 *
 * @author Jakub Smrha
 * @since 1.0
 */
@EqualsAndHashCode
public class DefaultArbitraryDataHolder implements ArbitraryDataHolder {
    protected final Map<String, Object> metadata = Collections.synchronizedMap(new HashMap<>());

    @Override
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Optional<V> getValue(final String key) throws ClassCastException {
        return (Optional<V>) Optional.ofNullable(metadata.get(key));
    }

    @Override
    public <V> V getValueUnsafe(final String key) throws NoSuchElementException, ClassCastException {
        final Optional<V> opt = getValue(key);
        return opt.orElseThrow(() -> new NoSuchElementException(key));
    }

    @Override
    public <V> V getValue(final String key, final V defaultValue) throws NoSuchElementException, ClassCastException {
        final Optional<V> opt = getValue(key);
        return opt.orElse(defaultValue);
    }

    @Override
    public void addMetadata(final String key, final Object value) {
        this.metadata.put(key, value);
    }

    @Override
    public void addMetadata(final Map<String, Object> metadata) {
        this.metadata.putAll(metadata);
    }

    @Override
    public boolean hasMetadataKey(final String key) {
        return metadata.containsKey(key);
    }

    @Override
    public void removeMetadata(final String key) {
        this.metadata.remove(key);
    }

    @Override
    public void clearMetadata() {
        this.metadata.clear();
    }

    @Override
    public String toString() {
        return metadata.toString();
    }
}
