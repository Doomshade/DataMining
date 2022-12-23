package cz.zcu.jsmahy.datamining.api.dbpedia;

import cz.zcu.jsmahy.datamining.api.AmbiguitySolver;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.RDFNode;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface DBPediaAmbiguitySolver<T extends RDFNode, R> extends AmbiguitySolver<T, R> {
    @Override
    AtomicReference<DataNode<T>> call(ObservableList<DataNode<T>> param, final RequestHandler<T, R> requestHandler);
}
