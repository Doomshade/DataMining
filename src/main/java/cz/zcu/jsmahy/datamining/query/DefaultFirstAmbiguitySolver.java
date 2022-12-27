package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.AmbiguitySolver;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReference;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.RDFNode;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DefaultFirstAmbiguitySolver<T extends RDFNode> implements AmbiguitySolver<T, Void> {

    @Override
    public DataNodeReference<T> call(final ObservableList<DataNode<T>> dataNodeList, final RequestHandler<T, Void> requestHandler) {
        final DataNodeReference<T> ref = new DataNodeReference<>();

        for (DataNode<T> dataNode : dataNodeList) {
            if (dataNode.getData()
                        .isURIResource()) {
                ref.set(dataNode);
                ref.setHasMultipleReferences(true);
                break;
            }
        }
        return ref;
    }
}
