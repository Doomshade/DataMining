package cz.zcu.jsmahy.datamining.api;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import cz.zcu.jsmahy.datamining.resolvers.MultipleItemChoiceResolver;
import cz.zcu.jsmahy.datamining.resolvers.OntologyPathPredicateResolver;
import cz.zcu.jsmahy.datamining.resolvers.StartAndEndDateResolver;
import cz.zcu.jsmahy.datamining.util.DialogHelper;

import static com.google.inject.Scopes.SINGLETON;

/**
 * The base module for this project.
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public abstract class DataMiningModule extends AbstractModule {

    /**
     * <p>NOTE: <b>DO NOT</b> require generic parameters because Guice is a blooooody safe DI framework that hates generics.</p>
     * <p>To clarify my frustration: Guice requires a typed implementation. The type MUST correspond to the required type. Once you use something like
     * {@code (...) class MyClass<T extends String> (...)} you are basically doomed and sentenced to using non-typed parameters because you can't do something like
     * {@code bind(new TypeLiteral<MyClass<String>>(){}.to(new TypeLiteral<MyClassImpl<String>() {};}. Not even wildcard arguments are valid, not even {@code MyClass<? extends String>}. So the only
     * way is to type it at runtime and tell Guice to skedaddle.</p>
     */
    @Override
    protected void configure() {
        bind(DataNodeFactory.class).to(DataNodeFactoryImpl.class)
                                   .in(SINGLETON);
        bind(DialogHelper.class).in(SINGLETON);
        bind(SparqlEndpointAgent.class).to(SparqlEndpointAgentImpl.class);

        // ambiguous input resolvers
        bind(ResponseResolver.class).annotatedWith(Names.named("userAssisted"))
                                    .to(MultipleItemChoiceResolver.class)
                                    .in(SINGLETON);
        bind(ResponseResolver.class).annotatedWith(Names.named("ontologyPathPredicate"))
                                    .to(OntologyPathPredicateResolver.class)
                                    .in(SINGLETON);
        bind(ResponseResolver.class).annotatedWith(Names.named("date"))
                                    .to(StartAndEndDateResolver.class)
                                    .in(SINGLETON);
    }
}
