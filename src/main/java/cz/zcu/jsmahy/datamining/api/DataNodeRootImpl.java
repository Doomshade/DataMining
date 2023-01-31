package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.function.BiConsumer;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true,
                   doNotUseGetters = true)
@ToString(callSuper = true,
          doNotUseGetters = true)
class DataNodeRootImpl extends DataNodeImpl implements DataNodeRoot {

    @Override
    public void iterate(BiConsumer<DataNode, Integer> biConsumer) {
        final ObservableList<DataNode> children = this.getChildren();
        if (!children.isEmpty()) {
            iterate(biConsumer, -1, this);
        }
    }

    @Override
    public DataNode getParent() {
        return null;
    }

    private void iterate(BiConsumer<DataNode, Integer> biConsumer, int depth, DataNode dataNode) {
        if (dataNode == null) {
            return;
        }

        if (!(dataNode instanceof DataNodeRoot)) {
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
}
