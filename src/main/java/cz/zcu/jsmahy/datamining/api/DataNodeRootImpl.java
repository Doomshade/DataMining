package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.function.BiConsumer;

@AllArgsConstructor
class DataNodeRootImpl<T> extends DataNodeImpl<T> implements DataNodeRoot<T> {
    /**
     * The display name of the root. If null then there's no display name.
     */
    @Getter
    @Setter
    private String name;

    @Override
    public void iterate(BiConsumer<DataNode<T>, Integer> biConsumer) {
        final ObservableList<DataNode<T>> children = this.getChildren();
        if (!children.isEmpty()) {
            iterate(biConsumer, -1, this);
        }
    }

    @Override
    public @NonNull T getData() {
        throw new UnsupportedOperationException("Root has no data");
    }

    private void iterate(BiConsumer<DataNode<T>, Integer> biConsumer, int depth, DataNode<T> dataNode) {
        if (dataNode == null) {
            return;
        }

        if (!(dataNode instanceof DataNodeRoot<T>)) {
            biConsumer.accept(dataNode, depth);
        }

        if (dataNode.hasChildren()) {
            depth++;
            for (final DataNode<T> node : dataNode.getChildren()) {
                iterate(biConsumer, depth, node);
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataNodeRootImpl)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final DataNodeRootImpl<?> that = (DataNodeRootImpl<?>) o;

        return getName() != null ? getName().equals(that.getName()) : that.getName() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
