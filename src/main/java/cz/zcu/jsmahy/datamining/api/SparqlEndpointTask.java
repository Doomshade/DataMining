package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

/**
 * <p>A SPARQL endpoint background task.</p>
 * <p>This abstraction exists mainly for testing purposes (it's easy to mock this class)</p>
 *
 * @param <R> The generic type of {@link Task}
 *
 * @see Task
 */
public abstract class SparqlEndpointTask<R> extends Task<R> {

    // TODO: docs
    public abstract void unlockDialogPane();

    public abstract ApplicationConfiguration getConfig();

    public abstract String getQuery();

    public abstract DataNode getDataNodeRoot();

    public abstract RequestProgressListener getProgressListener();

    // public for testing purposes
    @Override
    public abstract R call() throws Exception;
}
