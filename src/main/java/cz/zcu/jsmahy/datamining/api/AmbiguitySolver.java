package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.query.RequestHandler;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.RDFNode;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Solves ambiguities of nodes where a list of them occurs.</p>
 * <p>An example would be a ruler having multiple descendents.</p>
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public interface AmbiguitySolver<T, R> {
    /**
     * <p>WARNING: when a null reference is returned from the {@link AtomicReference} the program waits until it receives a reference.</p>
     * <p>The reference can be set any time</p>
     * <p>TODO: add comment</p>
     *
     * @param param          The list of {@link RDFNode}s to choose the result from
     * @param requestHandler the request handler
     *
     * @return an atomic reference
     */
    DataNodeReference<T> call(ObservableList<DataNode<T>> param, final RequestHandler<T, R> requestHandler);
}
