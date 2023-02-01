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
import java.util.function.BiConsumer;


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

    DataNodeImpl() {
        this(null);
    }

    DataNodeImpl(final DataNode parent) {
        this.parent = parent;
    }

    /**
     * Adds a child to this node.
     *
     * @param child the child to add to this node
     *
     * @throws NullPointerException if the child is {@code null}
     */
    void addChild(@NonNull DataNode child) throws NullPointerException {
        assert !child.isRoot();
        this.children.add(child);
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
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
    public Optional<DataNode> findRoot() {
        LOGGER.debug("Parent: {}", parent);
        DataNode prev = parent;
        while (prev != null && prev.getParent() != null) {
            prev = prev.getParent();
            LOGGER.debug("Searching deeper: {}", prev);
        }
        if (prev != null) {
            assert prev.isRoot(); // the upmost parent should always be root
            return Optional.of(prev);
        }
        return Optional.empty();
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public void iterate(BiConsumer<DataNode, Integer> biConsumer) {
        final ObservableList<DataNode> children = this.getChildren();
        if (!children.isEmpty()) {
            iterate(biConsumer, -1, this);
        }
    }

    private void iterate(BiConsumer<DataNode, Integer> biConsumer, int depth, DataNode dataNode) {
        if (dataNode == null) {
            return;
        }

        if (!dataNode.isRoot()) {
            biConsumer.accept(dataNode, depth);
        }

        final ObservableList<DataNode> children = dataNode.getChildren();
        if (!children.isEmpty()) {
            depth++;
            for (final DataNode node : children) {
                iterate(biConsumer, depth, node);
            }
        }
    }

    @Override
    public Iterator<DataNode> iterator() {
        return children.iterator();
    }
}
