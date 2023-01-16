package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.Restriction;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Collection;

/**
 * <p>Solves ambiguities of nodes where a list of them occurs.</p>
 * <p>An example would be a ruler having multiple descendents.</p>
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public interface DefaultAmbiguousInputResolver<T, R> extends AmbiguousInputResolver<T, R, DataNodeReferenceHolder<T>> {
    /**
     * <p>WARNING: the program waits until {@link DataNodeReferenceHolder#isFinished()} returns true</p>
     * <p>The reference can be set any time. Once the reference is set, you are obliged to resolveRequest {@link DataNodeReferenceHolder#finish()} to mark the reference as set followed by
     * {@link RequestHandler#unlockDialogPane()} if this method runs in a separate. If you do not resolveRequest these methods the program will get stuck because it {@code wait}s for the user input if
     * you decide to run this in a different thread (e.g. in a {@link Platform#runLater(Runnable)} resolveRequest).
     * </p>
     * <p>An example async (different thread) implementation:</p>
     * <pre>{@code
     * public class UserAssistedAmbiguitySolver<T extends RDFNode> implements AmbiguitySolver<T, Void> {
     *     @Override
     *     public DataNodeReferenceHolder<T> resolveRequest(ObservableList<DataNode<T>> list, RequestHandler<T, Void> requestHandler) {
     *         DataNodeReferenceHolder<T> ref = new DataNodeReferenceHolder<>();
     *         Platform.runLater(() -> {
     *           final Dialog<DataNode<T>> dialog = ...
     *           final List<DataNode<T>> result = dialog.showAndWait()
     *                                                 .orElse(null);
     *           ref.set(result);
     *           ref.finish();
     *           requestHandler.unlockDialogPane();
     *         });
     *         return ref;
     *     }
     * }
     * }
     * </pre>
     *
     * <p>An example sync implementation:</p>
     * <pre>{@code
     * @Override
     * public class DefaultFirstAmbiguitySolver<T extends RDFNode> implements AmbiguitySolver<T, Void> {
     *
     *     @Override
     *     public DataNodeReferenceHolder<T> resolveRequest(final ObservableList<DataNode<T>> dataNodeList, final RequestHandler<T, Void> requestHandler) {
     *         final DataNodeReferenceHolder<T> ref = new DataNodeReferenceHolder<>();
     *
     *         for (DataNode<T> dataNode : dataNodeList) {
     *             if (dataNode.getData()
     *                         .isURIResource()) {
     *                 ref.set(dataNode);
     *                 break;
     *             }
     *         }
     *         return ref;
     *     }
     * }
     *     }
     * </pre>
     *
     * @param param                 The list of {@link RDFNode}s to choose the result from
     * @param requestHandler        the request handler
     * @param ontologyPathPredicate
     * @param restrictions
     * @param model
     *
     * @return an atomic reference
     */
    DataNodeReferenceHolder<T> resolveRequest(ObservableList<DataNode<T>> param, final RequestHandler<T, R> requestHandler, final Property ontologyPathPredicate,
                                              final Collection<Restriction> restrictions,
                                              final Model model);
}
