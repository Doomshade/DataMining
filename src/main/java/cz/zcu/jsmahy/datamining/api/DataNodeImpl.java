package cz.zcu.jsmahy.datamining.api;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true,
          exclude = "parent")
class DataNodeImpl implements DataNode {
    private static final Logger LOGGER = LogManager.getLogger(DataNodeImpl.class);
    private static long ID_SEQ = 0;

    private final DataNode parent;
    private final long id;
    private final ObservableList<DataNode> children = FXCollections.observableArrayList();
    private final Map<String, Object> metadata = new HashMap<>();

    {
        this.id = ID_SEQ++;
    }

    protected DataNodeImpl() {
        this.parent = null;
    }

    DataNodeImpl(final DataNode parent) {
        this.parent = parent;
    }

    /**
     * Adds a child to this node.
     *
     * @param child the child to add to this node
     *
     * @throws IllegalArgumentException if the child is an instance of {@link DataNodeRoot}
     * @throws NullPointerException     if the child is {@code null}
     */
    public void addChild(@NonNull DataNode child) throws IllegalArgumentException, NullPointerException {
        assert !(child instanceof DataNodeRoot);
        this.children.add(child);
    }

    @Override
    public ObservableList<DataNode> getChildren() {
        return FXCollections.unmodifiableObservableList(children);
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
    public Optional<DataNodeRoot> findRoot() {
        LOGGER.debug("Parent: {}", parent);
        DataNode prev = parent;
        while (prev != null && prev.getParent() != null) {
            prev = prev.getParent();
            LOGGER.debug("Searching deeper: {}", prev);
        }
        if (prev != null) {
            assert prev instanceof DataNodeRoot; // the upmost parent should always be root
            return Optional.of((DataNodeRoot) prev);
        }
        return Optional.empty();
    }

    @Override
    public Iterator<DataNode> iterator() {
        return children.iterator();
    }
}
