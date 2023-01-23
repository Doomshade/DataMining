package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.QueryData;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.api.DefaultAmbiguousInputResolver;
import org.apache.jena.rdf.model.RDFNode;

import java.util.List;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DefaultFirstAmbiguousInputResolver<T extends RDFNode> implements DefaultAmbiguousInputResolver<T, Void> {

    @Override
    public DataNodeReferenceHolder<T> resolveRequest(final List<DataNode<T>> ambiguousInput, final QueryData inputMetadata, final RequestHandler<T, Void> requestHandler) {
        final DataNodeReferenceHolder<T> ref = new DataNodeReferenceHolder<>();

        for (DataNode<T> dataNode : ambiguousInput) {
            if (dataNode.getData()
                        .isURIResource()) {
                ref.set(dataNode);
                break;
            }
        }
        return ref;
    }
}
