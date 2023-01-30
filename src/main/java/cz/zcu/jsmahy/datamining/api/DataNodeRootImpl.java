package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.function.BiConsumer;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true,
                   doNotUseGetters = true)
class DataNodeRootImpl<T> extends DataNodeImpl<T> implements DataNodeRoot<T> {

    @Override
    public void iterate(BiConsumer<DataNode<T>, Integer> biConsumer) {
        final ObservableList<DataNode<T>> children = this.getChildren();
        if (!children.isEmpty()) {
            iterate(biConsumer, -1, this);
        }
    }

    @Override
    public T getData() {
        throw new UnsupportedOperationException("Root has no data");
    }

    @Override
    public DataNode<T> getParent() {
        return null;
    }

    private void iterate(BiConsumer<DataNode<T>, Integer> biConsumer, int depth, DataNode<T> dataNode) {
        if (dataNode == null) {
            return;
        }

        if (!(dataNode instanceof DataNodeRoot<T>)) {
            biConsumer.accept(dataNode, depth);
        }

        final ObservableList<DataNode<T>> children = dataNode.getChildren();
        if (!children.isEmpty()) {
            depth++;
            for (final DataNode<T> node : children) {
                iterate(biConsumer, depth, node);
            }
        }
    }
}
