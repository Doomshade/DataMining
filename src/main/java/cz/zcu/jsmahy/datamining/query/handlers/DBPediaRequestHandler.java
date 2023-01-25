package cz.zcu.jsmahy.datamining.query.handlers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.config.DBPediaConfiguration;
import cz.zcu.jsmahy.datamining.config.DataMiningConfiguration;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import cz.zcu.jsmahy.datamining.query.Restriction;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * The DBPedia {@link RequestHandler}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaRequestHandler<T extends RDFNode, R extends Void> extends AbstractRequestHandler<T, R> {
    //<editor-fold desc="Constants">
    private static final Logger LOGGER = LogManager.getLogger(DBPediaRequestHandler.class);
    private static final String DBPEDIA_SITE = "http://dbpedia.org/resource/";
    /**
     * <p>This comparator ensures the URI resources are placed first over literal resources.</p>
     * <p>The reasoning behind is simple: if the user wishes to fast forward when searching down the line we need to iterate through URI resources first.</p>
     */
    private static final Comparator<Statement> STATEMENT_COMPARATOR = (x, y) -> Boolean.compare(x.getObject()
                                                                                                 .isURIResource(),
                                                                                                y.getObject()
                                                                                                 .isURIResource());


    private final Collection<String> ignoredPredicates = new ArrayList<>();
    //</editor-fold>

    private final Collection<String> usedURIs = new HashSet<>();


    @Inject
    @SuppressWarnings("rawtypes")
    public DBPediaRequestHandler(final RequestProgressListener progressListener,
                                 final DataNodeFactory nodeFactory,
                                 final @Named("userAssisted") AmbiguousInputResolver ambiguousInputResolver,
                                 final @Named("ontologyPathPredicate") AmbiguousInputResolver ontologyPathPredicateInputResolver,
                                 final @Named("dbpediaConfig") DataMiningConfiguration configuration) {
        super(progressListener, nodeFactory, ambiguousInputResolver, ontologyPathPredicateInputResolver, configuration);
        if (configuration instanceof DBPediaConfiguration dbPediaConfiguration) {
            this.ignoredPredicates.addAll(dbPediaConfiguration.getIgnoredPredicates());
            LOGGER.info("Ignored predicates: " + ignoredPredicates);
        }
    }

    @Override
    protected synchronized R internalQuery() throws InvalidQueryException {
        final String requestPage = DBPEDIA_SITE.concat(query);
        final OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        final QueryData inputMetadata = new QueryData();
        try {
            LOGGER.info("Requesting {} for initial information.", requestPage);
            model.read(requestPage);
            inputMetadata.setCurrentModel(model);
        } catch (HttpException e) {
            throw LOGGER.throwing(e);
        } catch (Exception e) {
            throw new InvalidQueryException(e);
        }

        final Resource subject = model.getResource(requestPage);

        inputMetadata.setInitialSubject(subject);
        inputMetadata.setRestrictions(new ArrayList<>());
        this.usedURIs.clear();

        LOGGER.info("Start searching");

        if (initialSearch(inputMetadata)) {
            final Selector selector = new SimpleSelector(subject, inputMetadata.getOntologyPathPredicate(), (RDFNode) null);
            search(inputMetadata, selector, nodeFactory, dataNodeRoot);
            LOGGER.info("Done searching");
            progressListener.onSearchDone();
        } else {
            LOGGER.info("Invalid createBackgroundService '{}' - no results were found.", query);
            progressListener.onInvalidQuery(query);
        }
        return null;
    }

    /**
     * TODO: make the return value an enum
     *
     * @return {@code true} if a statement was found with the given subject (aka the createBackgroundService), {@code false} otherwise
     */
    private boolean initialSearch(final QueryData inputMetadata) {
        final Selector selector = new SimpleSelector(inputMetadata.getInitialSubject(), null, null, "en") {
            @Override
            public boolean selects(final Statement s) {
                final Property predicate = s.getPredicate();
                for (String ignoredPredicate : ignoredPredicates) {
                    if (predicate.hasURI(ignoredPredicate)) {
                        return false;
                    }
                }
                return true;
            }
        };
        final Model model = inputMetadata.getCurrentModel();

        final StmtIterator stmtIterator = model.listStatements(selector);
        if (!stmtIterator.hasNext()) {
            return false;
        }
        inputMetadata.setCandidatesForOntologyPathPredicate(stmtIterator);

        final DataNodeReferenceHolder<T> ref = ontologyPathPredicateInputResolver.resolveRequest(null, inputMetadata, this);
        if (ref instanceof BlockingDataNodeReferenceHolder<T> blockingRef) {
            while (!blockingRef.isFinished()) {
                try {
                    wait(5000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (ref == null) {
            return false;
        }
        final Property ontologyPathPredicate = ref.getOntologyPathPredicate();
        if (ontologyPathPredicate == null) {
            return false;
        }
        inputMetadata.setOntologyPathPredicate(ontologyPathPredicate);
        progressListener.onSetOntologyPathPredicate(ontologyPathPredicate);
        return true;
    }


    /**
     * Recursively searches based on the given model, selector, and a previous link. Adds the subject of the selector to the tree.
     *
     * @param selector     the selector
     * @param nodeFactory  the data node factory
     * @param dataNodeRoot the tree root
     */
    @SuppressWarnings("unchecked")
    private void search(final QueryData inputMetadata, final Selector selector, final DataNodeFactory<T> nodeFactory, final DataNodeRoot<T> dataNodeRoot) {
        final Model model = inputMetadata.getCurrentModel();

        final DataNode<T> curr = nodeFactory.newNode((T) selector.getSubject(), null);
        dataNodeRoot.addChild(curr);
        progressListener.onAddNewDataNode(curr, dataNodeRoot);

        final List<Statement> statements = model.listStatements(selector)
                                                .toList();
        statements.sort(STATEMENT_COMPARATOR);

        final List<DataNode<T>> foundData = new ArrayList<>();
        T previous = null;
        for (final Statement stmt : statements) {
            final T next = (T) stmt.getObject();
            final boolean meetsRequirements;
            try {
                meetsRequirements = meetsRequirements(inputMetadata, previous, next);
                previous = next;

                if (!meetsRequirements) {
                    return;
                }
            } catch (AssertionError e) {
                LOGGER.error("An internal error occurred when trying to check for the requirements of the node {}.", next, e);
                return;
            }

            final DataNode<T> nextNode = nodeFactory.newNode(next, null);
            LOGGER.debug("Found {}", next);
            foundData.add(nextNode);
        }

        // no nodes found, stop searching
        if (foundData.isEmpty()) {
            return;
        }

        // only one found, that means it's going linearly
        // we can continue searching
        if (foundData.size() == 1) {
            final DataNode<T> first = foundData.get(0);
            searchFurther(inputMetadata, nodeFactory, first, dataNodeRoot);
            return;
        }

        // multiple children found, that means we need to branch out
        // the ambiguity solver might pop up a dialogue where it could wait
        // for the response of the user
        // the dialogue is then responsible for notifying the monitor of this object
        // to free this thread via #unlockDialogPane method
        // the thread will wait up to 5 seconds and check for the result if the
        // dialogue fails to notify the monitor
        // the dialogue also has to be marked as finished
        // --------------
        // the reference can either be non-blocking or blocking, depending on the implementation
        // usually when user input is required it's blocking
        final DataNodeReferenceHolder<T> ref = ambiguousInputResolver.resolveRequest(foundData, inputMetadata, this);
        if (ref instanceof BlockingDataNodeReferenceHolder<T> blockingRef) {
            while (!blockingRef.isFinished()) {
                try {
                    wait(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        final DataNode<T> chosenDataNode = ref.get();
        if (chosenDataNode == null) {
            return;
        }

        // WARN: Deleted handling for multiple references as it might not even be in the final version.
        progressListener.onAddMultipleDataNodes(curr, foundData, chosenDataNode);
        searchFurther(inputMetadata, nodeFactory, chosenDataNode, dataNodeRoot);
    }

    private void searchFurther(final QueryData inputMetadata, final DataNodeFactory<T> nodeFactory, final DataNode<T> next, final DataNodeRoot<T> dataNodeRoot) {
        // attempts to search further down the line if the given data is a URI resource
        // if it's not a URI resource the searching terminates
        // if it's a URI resource we first check if we've been here already - we don't want to be stuck in a cycle,
        // that's what usedURIs is for
        // otherwise build a selector and continue searching down the line
        if (next == null) {
            return;
        }
        final T data = next.getData();
        if (!data.isURIResource()) {
            return;
        }
        final Model model = inputMetadata.getCurrentModel();
        final Resource resource = (Resource) data;
        readResource(model, resource);
        final boolean hasBeenVisited = !usedURIs.add(resource.getURI());
        if (!hasBeenVisited) {
            final Selector sel = new SimpleSelector(resource, inputMetadata.getOntologyPathPredicate(), (RDFNode) null);
            search(inputMetadata, sel, nodeFactory, dataNodeRoot);
        }
    }

    /**
     * <p>Checks whether the {@code curr}ent node meets the requirements to be added to the search list.</p>
     * <p><b>IMPORTANT:</b> this method reads the resource to the model and:</p>
     * <ul>
     *     <li><b>STAYS</b> there if this method returns <b>{@code true}</b>.</li>
     *     <li><b>ROLLS BACK</b> to the {@code previous} node if this method returns <b>{@code false}</b>.</li>
     * </ul>
     *
     * @param previous the previous node
     * @param curr     the current node
     *
     * @return Whether the current node meets requirements. Read this method's javadoc for more information regarding the changes this method potentially makes to the model.
     *
     * @throws IllegalStateException if the previous is not a URI resource
     */
    private boolean meetsRequirements(final QueryData inputMetadata, final T previous, final T curr) throws IllegalArgumentException {
        if (!curr.isURIResource()) {
            return true;
        }

        final Model model = inputMetadata.getCurrentModel();
        final Resource resource = curr.asResource();
        readResource(model, resource);

        // check for the restrictions on the given request
        for (final Restriction restriction : inputMetadata.getRestrictions()) {
            final Property predicate = model.getProperty(restriction.getNamespace(), restriction.getLink());
            final Selector sel = new SimpleSelector(resource, predicate, (RDFNode) null);
            final boolean foundSomething = model.listStatements(sel)
                                                .hasNext();
            if (!foundSomething) {
                // NOTE: previous HAS to be a resource because it's the curr's parent, so we don't need to check whether it's a resource
                // the only way to get to curr is for its parent to be a resource
                if (previous != null) {
                    readResource(model, previous.asResource());
                }
                return false;
            }
        }
        return true;
    }

    private void readResource(final Model model, final Resource resource) {
        String uri = resource.getURI();
        model.read(uri);
    }

}
