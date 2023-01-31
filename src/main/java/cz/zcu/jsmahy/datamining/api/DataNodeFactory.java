package cz.zcu.jsmahy.datamining.api;

import lombok.NonNull;
import org.apache.jena.rdf.model.RDFNode;

import static cz.zcu.jsmahy.datamining.util.RDFNodeUtil.SPECIAL_CHARACTERS;
import static cz.zcu.jsmahy.datamining.util.RDFNodeUtil.formatRDFNode;

/**
 * A factory for {@link DataNode}s.
 *
 * @author Jakub Šmrha
 * @see DataNode
 * @see DataNodeRoot
 * @since 1.0
 */
public class DataNodeFactory<T> {

    /**
     * Creates a new root.
     *
     * @param rootName the root display name
     *
     * @return a data node root
     */
    public DataNodeRoot<T> newRoot(final @NonNull String rootName) {
        final DataNodeRoot<T> root = new DataNodeRootImpl<>();
        root.setName(rootName);
        return root;
    }

    /**
     * Creates a new data node with the given data.
     *
     * @param data   the data
     * @param parent the data node's parent
     *
     * @return a new data node
     */
    public DataNode<T> newNode(final @NonNull T data, final @NonNull DataNode<T> parent) {
        final DataNode<T> dataNode = new DataNodeImpl<>(data, parent);
        // implicitly set the name of the data node to its formatted state if it's an RDFNode implementation
        if (data instanceof RDFNode rdfNode) {
            dataNode.setName(formatRDFNode(rdfNode).replaceAll(SPECIAL_CHARACTERS, " "));
            if (rdfNode.isURIResource()) {
                dataNode.setUri(rdfNode.asResource()
                                       .getURI());
            }
        }
        ((DataNodeImpl<T>) parent).addChild(dataNode);
        return dataNode;
    }
}
