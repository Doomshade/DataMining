package cz.zcu.jsmahy.datamining.api;

import lombok.NonNull;

import java.util.Map;

/**
 * A factory for {@link DataNode}s.
 *
 * @author Jakub Šmrha
 * @see DataNode
 * @see DataNodeRoot
 * @since 1.0
 */
public class DataNodeFactory {

    /**
     * Creates a new root.
     *
     * @param rootName the root display name
     *
     * @return a data node root
     */
    public DataNodeRoot newRoot(final @NonNull String rootName) {
        final DataNodeRoot root = new DataNodeRootImpl();
        root.addMetadata("name", rootName);
        return root;
    }

    // TODO: javadocs
    public DataNode newNode(final @NonNull DataNode parent) {
        final DataNode dataNode = new DataNodeImpl(parent);
        ((DataNodeImpl) parent).addChild(dataNode);
        return dataNode;
    }

    public DataNode newNode(final @NonNull DataNode parent, String name) {
        return newNode(parent, "name", name);
    }


    public DataNode newNode(final @NonNull DataNode parent, String metadataKey, Object metadataValue) {
        final DataNode newNode = newNode(parent);
        newNode.addMetadata(metadataKey, metadataValue);
        return newNode;
    }

    public DataNode newNode(final @NonNull DataNode parent, Map<String, Object> metadata) {
        final DataNode newNode = newNode(parent);
        newNode.addMetadata(metadata);
        return newNode;
    }


}
