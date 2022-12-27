package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReference;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaAmbiguitySolver;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.RDFNode;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DefaultAllAmbiguitySolver<T extends RDFNode> implements DBPediaAmbiguitySolver<T, Void> {

    @Override
    public DataNodeReference<T> call(final ObservableList<DataNode<T>> dataNodeList, final RequestHandler<T, Void> requestHandler) {
        final DataNodeReference<T> ref = new DataNodeReference<>();
        ref.setHasMultipleReferences(true);
        return ref;
    }
}
