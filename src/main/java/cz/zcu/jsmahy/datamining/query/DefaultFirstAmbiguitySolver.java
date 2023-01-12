package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.AmbiguitySolver;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReferenceHolder;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Collection;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DefaultFirstAmbiguitySolver<T extends RDFNode> implements AmbiguitySolver<T, Void> {

    @Override
    public DataNodeReferenceHolder<T> call(final ObservableList<DataNode<T>> dataNodeList, final RequestHandler<T, Void> requestHandler, final Property ontologyPathPredicate,
                                           final Collection<Restriction> restrictions, final Model model) {
        final DataNodeReferenceHolder<T> ref = new DataNodeReferenceHolder<>();

        for (DataNode<T> dataNode : dataNodeList) {
            if (dataNode.getData()
                        .isURIResource()) {
                ref.set(dataNode);
                break;
            }
        }
        return ref;
    }
}
