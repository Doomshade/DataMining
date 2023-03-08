package cz.zcu.jsmahy.datamining.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;


@Data
@EqualsAndHashCode(callSuper = false,
                   doNotUseGetters = true)
@ToString(doNotUseGetters = true,
          exclude = "parent",
          callSuper = true)
final class DataNodeImpl extends DefaultArbitraryDataHolder implements DataNode {
    private static final Logger LOGGER = LogManager.getLogger(DataNodeImpl.class);
    private static long ID_SEQ = 0;
    /**
     * Using implementation because of Jackson. Jackson needs a default empty constructor, so we make a wrapper of... the existing wrapper... because that one does not permit empty lists...
     */
    private final ObservableListWrapperWrapper<DataNode> children = new ObservableListWrapperWrapper<>();
    // the parent could be removed from its parent's children, thus losing a reference to it
    //  R
    //  ├── A
    //  ├── B
    //  │   ├── C
    //  │   ├── D
    //  │   └── E
    //  ├── F
    //  │   └── G
    //  └── H
    // for example this node is E, and its parent (B) is removed from its parent (R). the tree then looks as follows:
    //  R
    //  ├── A
    //  │
    //  │   ┌── C -- B (ref to parent)
    //  │   ├── D -- B (ref to parent)
    //  │   └── E -- B (ref to parent)
    //  │
    //  ├── F
    //  │   └── G
    //  └── H
    // but this node (as well as nodes C and D) still hold reference to B that's no longer present in the tree, basically (temporarily) leaking memory because
    // we are unable to delete B
    // the nodes C, D, E will be deleted in the first GC cycle, whereas the B node will be deleted in the second GC cycle
    @JsonIgnore
    private transient WeakReference<DataNode> parent;
    // non-final to have it deserializable
    private long id;

    {
        this.id = ID_SEQ++;
    }

    DataNodeImpl() {
        this(null);
    }

    DataNodeImpl(final DataNode parent) {
        this.parent = new WeakReference<>(parent);
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
    public DataNode getParent() {
        return parent.get();
    }

    void setParent(DataNode parent) {
        this.parent = new WeakReference<>(parent);
    }

    @Override
    public Optional<? extends DataNode> findRoot() {
        DataNode prev = parent.get();
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
        return getParent() == null;
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
    @NotNull
    public Iterator<DataNode> iterator() {
        return children.iterator();
    }
}
