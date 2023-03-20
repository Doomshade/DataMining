package cz.zcu.jsmahy.datamining.api;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import cz.zcu.jsmahy.datamining.app.controller.MainController;

import static com.google.inject.Scopes.SINGLETON;

/**
 * <p>The base module for this project.</p>
 * <p>NOTE: <b>DO NOT</b> require generic parameters because Guice is a blooooody safe DI framework that hates generics.</p>
 * <p>To clarify <i><s>my frustration</s></i>: Guice requires a typed implementation. The type MUST correspond to the required type. Once you use something like
 * <pre>{@code
 * public class MyClass<T extends String> { (...) }
 * }</pre>
 * you are basically doomed and sentenced to using non-typed parameters because you can't do something like
 * <pre>{@code
 * bind(new TypeLiteral<MyClass<String>>(){}.to(new TypeLiteral<MyClassImpl<String>() {};
 * }</pre>
 * <p>
 * Wildcard arguments are invalid, not even something like
 * <pre>{@code
 * MyClass<? extends String>
 * }</pre>
 * So the only way is to type it at runtime and tell Guice to skedaddle.</p>
 *
 * <p>An example of the <s>right</s> way of doing it:
 * <pre>{@code
 *   private final SparqlEndpointTaskProvider<T, R, ApplicationConfiguration<T, R>> sparqlEndpointTaskProvider;
 *   private final ApplicationConfiguration<T, R> config;
 *   private final DataNodeFactory<T> nodeFactory;
 *
 *   @Inject
 *   @SuppressWarnings("unchecked, rawtypes")
 *   public SparqlEndpointAgent(final ApplicationConfiguration config, final DataNodeFactory nodeFactory, final SparqlEndpointTaskProvider sparqlEndpointTaskProvider) {
 *     this.config = requireNonNull(config);
 *     this.nodeFactory = requireNonNull(nodeFactory);
 *     this.sparqlEndpointTaskProvider = requireNonNull(sparqlEndpointTaskProvider);
 *   }
 * }</pre>
 * <p>
 * Worst that could happen is some {@link RuntimeException} being thrown because of the wrong type arguments.</p>
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public class DataMiningModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DataNodeFactory.class).in(SINGLETON);
        bind(ApplicationConfiguration.class).to(DefaultApplicationConfiguration.class);
        bind(JSONDataNodeSerializationUtils.class).in(SINGLETON);
        bind(SparqlQueryServiceHolder.class).to(MainController.class);
        bind(DataNodeSerializer.class).annotatedWith(Names.named("builtin"))
                                      .to(JSONDataNodeSerializer.class)
                                      .asEagerSingleton();
        bind(DataNodeDeserializer.class).annotatedWith(Names.named("builtin"))
                                        .to(JSONDataNodeDeserializer.class)
                                        .asEagerSingleton();
//        bind(MainController.class).asEagerSingleton();
    }
}
