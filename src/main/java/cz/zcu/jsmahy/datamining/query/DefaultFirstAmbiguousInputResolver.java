package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.AmbiguousInputMetadata;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.api.DefaultAmbiguousInputResolver;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.RDFNode;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DefaultFirstAmbiguousInputResolver<T extends RDFNode> implements DefaultAmbiguousInputResolver<T, Void> {

    @Override
    public DataNodeReferenceHolder<T> resolveRequest(final ObservableList<DataNode<T>> ambiguousInput, final AmbiguousInputMetadata<T, Void> inputMetadata) {
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
