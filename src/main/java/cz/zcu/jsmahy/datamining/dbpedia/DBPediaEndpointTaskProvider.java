package cz.zcu.jsmahy.datamining.dbpedia;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.zcu.jsmahy.datamining.api.*;

import static java.util.Objects.requireNonNull;

public class DBPediaEndpointTaskProvider<R> implements SparqlEndpointTaskProvider<R> {

    private final ApplicationConfiguration config;
    private final RequestProgressListener progressListener;
    private final DataNodeFactory dataNodeFactory;
    private final ResponseResolver<?> ambiguousResultResolver;
    private final ResponseResolver<?> ontologyPathPredicateResolver;
    private final ResponseResolver<?> startAndEndDateResolver;

    @Inject
    @SuppressWarnings("rawtypes")
    public DBPediaEndpointTaskProvider(final ApplicationConfiguration config,
                                       final RequestProgressListener progressListener,
                                       final DataNodeFactory dataNodeFactory,
                                       final @Named("userAssisted") ResponseResolver ambiguousResultResolver,
                                       final @Named("ontologyPathPredicate") ResponseResolver ontologyPathPredicateResolver,
                                       final @Named("date") ResponseResolver startAndEndDateResolver) {
        this.config = requireNonNull(config);
        this.progressListener = requireNonNull(progressListener);
        this.dataNodeFactory = requireNonNull(dataNodeFactory);
        this.ambiguousResultResolver = requireNonNull(ambiguousResultResolver);
        this.ontologyPathPredicateResolver = requireNonNull(ontologyPathPredicateResolver);
        this.startAndEndDateResolver = requireNonNull(startAndEndDateResolver);
    }

    @Override
    public SparqlEndpointTask<R> newTask(final String query, final DataNode dataNodeRoot) {
        return new DBPediaEndpointTask<>(query, dataNodeRoot, config, progressListener, dataNodeFactory, ambiguousResultResolver, ontologyPathPredicateResolver, startAndEndDateResolver);
    }
}
