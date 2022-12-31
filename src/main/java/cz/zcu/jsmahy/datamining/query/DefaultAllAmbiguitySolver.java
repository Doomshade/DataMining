package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaAmbiguitySolver;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.RDFNode;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DefaultAllAmbiguitySolver<T extends RDFNode> implements DBPediaAmbiguitySolver<T, Void> {

    @Override
    public DataNodeReferenceHolder<T> call(final ObservableList<DataNode<T>> dataNodeList, final RequestHandler<T, Void> requestHandler) {
        final DataNodeReferenceHolder<T> ref = new DataNodeReferenceHolder<>();
        return ref;
    }
}
