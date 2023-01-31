package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

/**
 * <p>A SPARQL endpoint background task.</p>
 * <p>This abstraction exists mainly for testing purposes (it's easy to mock this class)</p>
 *
 * @param <T> The data type of {@link DataNode}
 * @param <R> The generic type of {@link Task}
 *
 * @see Task
 */
public abstract class SparqlEndpointTask<T, R> extends Task<R> {

    public abstract void unlockDialogPane();

    public abstract ApplicationConfiguration<T, R> getConfig();

    public abstract DataNodeFactory<T> getDataNodeFactory();

    public abstract String getQuery();

    public abstract DataNodeRoot<T> getDataNodeRoot();

    // public for testing purposes
    @Override
    public abstract R call() throws Exception;
}
