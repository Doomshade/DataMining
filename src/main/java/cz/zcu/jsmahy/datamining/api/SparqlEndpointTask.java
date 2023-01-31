package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class SparqlEndpointTask<T, R, CFG extends ApplicationConfiguration<T, R>> extends Task<R> {
    protected final CFG config;
    protected final DataNodeFactory<T> nodeFactory;
    protected final String query;
    protected final DataNodeRoot<T> dataNodeRoot;

    public SparqlEndpointTask(final CFG config, final DataNodeFactory<T> nodeFactory, String query, final DataNodeRoot<T> dataNodeRoot) {
        this.config = requireNonNull(config);
        this.nodeFactory = requireNonNull(nodeFactory);
        this.dataNodeRoot = requireNonNull(dataNodeRoot);

        query = requireNonNull(query);
        final String baseUrl = requireNonNull(config.getBaseUrl());
        final boolean hasBaseUrl = query.startsWith(baseUrl);
        if (hasBaseUrl) {
            this.query = query;
        } else {
            this.query = baseUrl.concat(query);
        }
    }

    public synchronized void unlockDialogPane() {
        notify();
    }
}
