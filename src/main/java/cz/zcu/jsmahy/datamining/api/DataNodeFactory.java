package cz.zcu.jsmahy.datamining.api;

import java.util.Map;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;

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
        root.addMetadata(METADATA_KEY_NAME, rootName);
        return root;
    }

    /**
     * Creates a new node and adds it to the parent. Calls {@link #newNode(DataNode, boolean)} with {@code true} as the second argument.
     *
     * @param parent the parent of the new node
     *
     * @return the new node
     */
    public DataNode newNode(final DataNode parent) {
        return newNode(parent, true);
    }

    /**
     * Creates a new node and adds it to the parent if {@code addChild} is {@code true}.
     *
     * @param parent   the parent
     * @param addChild whether to add this node to the parent
     *
     * @return the new node
     */
    public DataNode newNode(final DataNode parent, boolean addChild) {
        final DataNode dataNode = new DataNodeImpl(parent);
        if (addChild) {
            ((DataNodeImpl) parent).addChild(dataNode);
        }
        return dataNode;
    }

    /**
     * Creates a new node, adds it to the parent, and adds an implicit name. Calls {@link #newNode(DataNode, String, Object)} with {@link DataNode#METADATA_KEY_NAME} and {@code name} as the second and
     * third argument, respectively.
     *
     * @param parent the parent to add this node to
     * @param name   the name of the node
     *
     * @return the new node
     */
    public DataNode newNode(final DataNode parent, String name) {
        return newNode(parent, METADATA_KEY_NAME, name);
    }

    /**
     * Creates a new node, adds it to the parent, and adds a metadata value stored under the metadata key.
     *
     * @param parent        the parent to add this node to
     * @param metadataKey   the metadata key
     * @param metadataValue the metadata value
     *
     * @return the new node
     */
    public DataNode newNode(final DataNode parent, String metadataKey, Object metadataValue) {
        final DataNode newNode = newNode(parent);
        newNode.addMetadata(metadataKey, metadataValue);
        return newNode;
    }

    /**
     * Creates a new node, adds it to the parent, and adds metadata key/value pairs.
     *
     * @param parent   the parent
     * @param metadata the metadata key/value pairs
     *
     * @return the new node
     */
    public DataNode newNode(final DataNode parent, Map<String, Object> metadata) {
        final DataNode newNode = newNode(parent);
        newNode.addMetadata(metadata);
        return newNode;
    }


}
