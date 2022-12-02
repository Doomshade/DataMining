package cz.zcu.jsmahy.datamining.api;

import java.util.function.BiConsumer;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public abstract class DataNodeRoot<T> extends DataNodeImpl<T> {

    DataNodeRoot(final T data) {
        super(data);
    }

    public void iterate(BiConsumer<DataNode<T>, String> biConsumer) {
        iterate(biConsumer, "", this);
    }

    private void iterate(BiConsumer<DataNode<T>, String> biConsumer, String indent, DataNode<T> dataNode) {
        if (dataNode != null) {
            biConsumer.accept(dataNode, indent);
            if (dataNode.hasNext()) {
                indent += "  ";
                for (final DataNode<T> node : dataNode.next()) {
                    iterate(biConsumer, indent, node);
                }
            }
        }
    }
}
