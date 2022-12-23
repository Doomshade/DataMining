package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.AmbiguitySolver;
import cz.zcu.jsmahy.datamining.api.DataNode;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.RDFNode;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DefaultFirstAmbiguitySolver<T extends RDFNode> implements AmbiguitySolver<T, Void> {

    @Override
    public AtomicReference<DataNode<T>> call(final ObservableList<DataNode<T>> dataNodeList, final RequestHandler<T, Void> requestHandler) {
        final AtomicReference<DataNode<T>> ref = new AtomicReference<>();
        DataNode<T> result = null;
        for (DataNode<T> dataNode : dataNodeList) {
            result = dataNode;
            if (dataNode.getData()
                        .isURIResource()) {
                break;
            }
        }
        ref.set(result);
        return ref;
    }
}
