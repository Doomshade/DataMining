package cz.zcu.jsmahy.datamining.api;

import org.apache.jena.rdf.model.RDFNode;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public interface ArbitraryDataHolder {
    /**
     * Corresponding value should be {@link String}
     */
    String METADATA_KEY_NAME = "name";
    /**
     * Corresponding value should be {@link String}
     */
    String METADATA_KEY_URI = "uri";
    /**
     * Corresponding value should be {@link RDFNode}
     */
    String METADATA_KEY_RDF_NODE = "rdfNode";
    /**
     * Corresponding value should be {@link String}
     */
    String METADATA_KEY_DESCRIPTION = "description";
    /**
     * Corresponding value should be {@link List} of {@link Relationship}s
     */
    String METADATA_KEY_RELATIONSHIPS = "relationships";

    /**
     * NOTE: the map is <b>unmodifiable</b>
     *
     * @return The metadata
     */
    Map<String, Object> getMetadata();

    /**
     * @param key the key
     * @param <V> the value type
     *
     * @return the value stored under the key
     *
     * @throws ClassCastException if the value type is incorrect
     */
    <V> Optional<V> getMetadataValue(String key) throws ClassCastException;

    /**
     * @param key the key
     * @param <V> the value type
     *
     * @return the value stored under the key
     *
     * @throws NoSuchElementException if no such key is mapped to a variable
     * @throws ClassCastException     if the value type is incorrect
     */
    <V> V getMetadataValueUnsafe(String key) throws NoSuchElementException, ClassCastException;

    /**
     * @param key          the key
     * @param defaultValue the default value if no such key is mapped to a variable
     * @param <V>          the value type
     *
     * @return the value stored under the key
     *
     * @throws ClassCastException if the value type is incorrect
     */

    <V> V getMetadataValue(String key, V defaultValue) throws ClassCastException;

    /**
     * Adds an additional metadata value stored under the key
     *
     * @param key   the key
     * @param value the value
     */
    void addMetadata(String key, Object value);

    /**
     * Adds additional metadata values
     *
     * @param metadata the metadata values
     */
    void addMetadata(Map<String, Object> metadata);

    /**
     * @param key the key
     *
     * @return Whether a value is stored under the key.
     */
    boolean hasMetadataKey(String key);
}
