package cz.zcu.jsmahy.datamining.api;

import java.util.Map;

/**
 * A factory for {@link DataNode}s.
 *
 * @author Jakub Šmrha
 * @see DataNode
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
    public DataNode newRoot(final String rootName) {
        final DataNode root = new DataNodeImpl();
        root.addMetadata("name", rootName);
        return root;
    }

    // TODO: javadocs
    public DataNode newNode(final DataNode parent) {
        return newNode(parent, true);
    }

    public DataNode newNode(final DataNode parent, boolean addChild) {
        final DataNode dataNode = new DataNodeImpl(parent);
        if (addChild) {
            ((DataNodeImpl) parent).addChild(dataNode);
        }
        return dataNode;
    }

    public DataNode newNode(final DataNode parent, String name) {
        return newNode(parent, "name", name);
    }


    public DataNode newNode(final DataNode parent, String metadataKey, Object metadataValue) {
        final DataNode newNode = newNode(parent);
        newNode.addMetadata(metadataKey, metadataValue);
        return newNode;
    }

    public DataNode newNode(final DataNode parent, Map<String, Object> metadata) {
        final DataNode newNode = newNode(parent);
        newNode.addMetadata(metadata);
        return newNode;
    }


}
