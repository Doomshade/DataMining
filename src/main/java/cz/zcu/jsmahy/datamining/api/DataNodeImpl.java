package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;


@Data
@EqualsAndHashCode(callSuper = false,
                   doNotUseGetters = true)
@ToString(doNotUseGetters = true,
          exclude = "parent")
class DataNodeImpl extends DefaultArbitraryDataHolder implements DataNode {
    private static final Logger LOGGER = LogManager.getLogger(DataNodeImpl.class);
    private static long ID_SEQ = 0;

    @JsonIgnore
    private final transient DataNode parent;
    // ISTG if I ever see the dude who made up the generics system in Java I'll eat his cookies
    /**
     * Using implementation because
     */
    private final ObservableListWrapperWrapper<DataNode> children = new ObservableListWrapperWrapper<>(FXCollections.observableArrayList());
    private long id;

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
    void addChild(DataNode child) throws NullPointerException {
        assert !child.isRoot();
        this.children.add(child);
    }

    @Override
    public ObservableList<? extends DataNode> getChildren() {
        return children;
    }

    @Override
    public Optional<? extends DataNode> findRoot() {
        DataNode prev = parent;
        while (prev != null && prev.getParent() != null) {
            prev = prev.getParent();
        }
        if (prev != null) {
            assert prev.isRoot(); // the upmost parent should always be root
            return Optional.of(prev);
        }
        return Optional.empty();
    }

    @Override
    @JsonIgnore
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public void iterate(BiConsumer<DataNode, Integer> biConsumer) {
        final ObservableList<? extends DataNode> children = this.getChildren();
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

        final ObservableList<? extends DataNode> children = dataNode.getChildren();
        if (!children.isEmpty()) {
            depth++;
            for (final DataNode node : children) {
                iterate(biConsumer, depth, node);
            }
        }
    }

    @Override
    public Iterator<DataNode> iterator() {
        // holy ****ing **** why does Java have to be so bad
        return children.iterator();
    }
}
