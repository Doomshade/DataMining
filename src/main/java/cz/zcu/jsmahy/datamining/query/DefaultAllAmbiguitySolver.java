package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaAmbiguitySolver;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.RDFNode;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DefaultAllAmbiguitySolver<T extends RDFNode> implements DBPediaAmbiguitySolver<T, Void> {

    @Override
    public AtomicReference<DataNode<T>> call(final ObservableList<DataNode<T>> dataNodeList, final RequestHandler<T, Void> requestHandler) {
        return new AtomicReference<>(null);
    }
}
