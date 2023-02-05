package cz.zcu.jsmahy.datamining.api;

import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * TODO: javadocs
 *
 * @author Jakub Smrha
 * @since 1.0
 */
@EqualsAndHashCode
class DefaultArbitraryDataHolder implements ArbitraryDataHolder {
    protected final Map<String, Object> metadata = new HashMap<>();

    @Override
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Optional<V> getMetadataValue(final String key) throws ClassCastException {
        return (Optional<V>) Optional.ofNullable(metadata.get(key));
    }

    @Override
    public <V> V getMetadataValueUnsafe(final String key) throws NoSuchElementException, ClassCastException {
        final Optional<V> opt = getMetadataValue(key);
        return opt.orElseThrow(() -> new NoSuchElementException(key));
    }

    @Override
    public <V> V getMetadataValue(final String key, final V defaultValue) throws NoSuchElementException, ClassCastException {
        final Optional<V> opt = getMetadataValue(key);
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
}
