package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;

import java.util.function.BiConsumer;


public class DataNodeRootImpl<T> extends DataNodeImpl<T> implements DataNodeRoot<T> {

    @Override
    public void iterate(BiConsumer<DataNode<T>, Integer> biConsumer) {
        final ObservableList<DataNode<T>> children = this.getChildren();
        if (!children.isEmpty()) {
            iterate(biConsumer, -1, children.get(0));
        }
    }

    private void iterate(BiConsumer<DataNode<T>, Integer> biConsumer, int depth, DataNode<T> dataNode) {
        if (dataNode != null) {
            biConsumer.accept(dataNode, depth);
            if (dataNode.hasChildren()) {
                depth++;
                for (final DataNode<T> node : dataNode.getChildren()) {
                    iterate(biConsumer, depth, node);
                }
            }
        }
    }
}
