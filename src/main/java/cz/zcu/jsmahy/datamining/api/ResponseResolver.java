package cz.zcu.jsmahy.datamining.api;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * <p>Solves ambiguities of nodes where a list of them occurs.</p>
 * <p>An example would be a monarch having multiple descendents.</p>
 *
 * @param <D> The data input
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public interface ResponseResolver<D> {
    /**
     * <p>WARNING: the program waits until {@link BlockingDataNodeReferenceHolder#isFinished()} returns true</p>
     * <p>The reference can be set any time. Once the reference is set, you are obliged to resolveRequest {@link BlockingDataNodeReferenceHolder#finish()} to mark the reference as set followed by
     * {@link SparqlEndpointTask#unlockDialogPane()} if this method runs in a separate. If you do not resolveRequest these methods the program will get stuck because it {@code wait}s for the user
     * input if you decide to run this in a different thread (e.g. in a {@link Platform#runLater(Runnable)} resolveRequest).
     * </p>
     * <p>An example async (different thread) implementation:</p>
     * <pre>{@code
     * public class UserAssistedAmbiguitySolver<T extends RDFNode> implements AmbiguitySolver<T, Void> {
     *     @Override
     *     public DataNodeReferenceHolder<T> resolveRequest(ObservableList<DataNode<T>> list, SparqlEndpointAgent<T, Void> requestHandler) {
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
     *     public DataNodeReferenceHolder<T> resolveRequest(final ObservableList<DataNode<T>> dataNodeList, final SparqlEndpointAgent<T, Void> requestHandler) {
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
     * @param inputMetadata  The query input metadata
     * @param requestHandler The request handler
     *
     * @throws IllegalStateException if another thread attempts to resolve a request by this resolver while it's running
     */
    void resolve(final D inputMetadata, final SparqlEndpointTask<?> requestHandler) throws IllegalStateException;

    /**
     * @return the result of the {@link ResponseResolver#resolve(D, SparqlEndpointTask)}
     *
     * @throws IllegalStateException if the {@link ResponseResolver#resolve(D, SparqlEndpointTask)} was not called prior to this method
     */
    ArbitraryDataHolder getResponse() throws IllegalStateException;

    /**
     * @return the response property
     *
     * @see ResponseResolver#hasResponseReady()
     */
    ReadOnlyBooleanProperty hasResponseReadyProperty();

    /**
     * This boolean should <b>always</b> be {@code true} if this resolver is not blocking
     *
     * @return {@code true} if the response is ready {@code false} otherwise
     */
    boolean hasResponseReady();

    /**
     * Marks this reference as finished -- the reference has been set.
     */
    void markResponseReady();
}
