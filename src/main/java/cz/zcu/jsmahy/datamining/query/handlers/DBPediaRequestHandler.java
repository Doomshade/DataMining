package cz.zcu.jsmahy.datamining.query.handlers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import cz.zcu.jsmahy.datamining.query.AbstractRequestHandler;
import cz.zcu.jsmahy.datamining.query.BlockingRequestHandler;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.Restriction;
import javafx.scene.control.TreeItem;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * The DBPedia {@link RequestHandler}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaRequestHandler<T extends RDFNode, R extends Void> extends AbstractRequestHandler<T, R> implements BlockingRequestHandler<T, R> {
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


    private static final Collection<String> IGNORED_PREDICATES = new ArrayList<>() {
        {
            add("http://dbpedia.org/ontology/wikiPageWikiLink");
            add("http://dbpedia.org/ontology/wikiPageExternalLink");
            add("http://www.w3.org/2002/07/owl#sameAs");
            add("http://dbpedia.org/property/wikiPageUsesTemplate");
            add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            add("http://dbpedia.org/ontology/abstract");
            add("http://purl.org/dc/terms/subject");
            add("http://www.w3.org/2000/01/rdf-schema#label");
            add("http://dbpedia.org/ontology/wikiPageRevisionID");
            add("http://xmlns.com/foaf/0.1/depiction");
            add("http://dbpedia.org/ontology/deathPlace");
            add("http://www.w3.org/2000/01/rdf-schema#comment");
            add("http://dbpedia.org/property/wikt");
            add("http://www.w3.org/2000/01/rdf-schema#comment");
            add("http://dbpedia.org/ontology/wikiPageLength");
            add("http://dbpedia.org/property/thesisTitle");
            add("http://dbpedia.org/ontology/thumbnail");
        }
    };

    private static final Function<Resource, Selector> PREDICATE_FILTER_SELECTOR = subject -> new SimpleSelector(subject, null, null, "en") {
        @Override
        public boolean selects(final Statement s) {
            final Property predicate = s.getPredicate();
            for (String ignoredPredicate : IGNORED_PREDICATES) {
                if (predicate.hasURI(ignoredPredicate)) {
                    return false;
                }
            }
            return true;
        }
    };
    //</editor-fold>

    private static boolean requesting = false;
    private final Collection<String> usedURIs = new HashSet<>();

    private final DataNodeFactory<T> nodeFactory;

    private final AmbiguousInputResolver<T, R, ?> ambiguousInputResolver;

    private final AmbiguousInputResolver<T, R, ?> ontologyPathPredicateInputResolver;

    @Inject
    @SuppressWarnings("unchecked, rawtypes")
    public DBPediaRequestHandler(final RequestProgressListener progressListener,
                                 final DataNodeFactory nodeFactory,
                                 final @Named("userAssisted") AmbiguousInputResolver ambiguousInputResolver,
                                 final @Named("ontologyPathPredicate") AmbiguousInputResolver ontologyPathPredicateInputResolver) {
        super(progressListener);
        this.nodeFactory = nodeFactory;
        this.ambiguousInputResolver = ambiguousInputResolver;
        this.ontologyPathPredicateInputResolver = ontologyPathPredicateInputResolver;
    }

    /**
     * Attempts to continue the search if the monitor is in wait queue.
     */
    public synchronized void unlockDialogPane() {
        notify();
    }

    @Override
    protected synchronized R internalQuery(final String query, final TreeItem<DataNode<T>> treeRoot) throws InvalidQueryException {
        Objects.requireNonNull(query);
        Objects.requireNonNull(treeRoot);
        if (requesting) {
            throw new IllegalStateException("Already requesting!");
        }
        requesting = true;

        final String requestPage = DBPEDIA_SITE.concat(query);
        final OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        final QueryData inputMetadata = new QueryData();
        try {
            LOGGER.info("Requesting {} for initial information.", requestPage);
            model.read(requestPage);
            inputMetadata.setModel(model);
        } catch (HttpException e) {
            requesting = false;
            throw LOGGER.throwing(e);
        } catch (Exception e) {
            requesting = false;
            throw new InvalidQueryException(e);
        }

        final Resource subject = model.getResource(requestPage);

        inputMetadata.setSubject(subject);
        inputMetadata.setRestrictions(new ArrayList<>());
        this.usedURIs.clear();

        LOGGER.info("Start searching");

        if (initialSearch(inputMetadata)) {
            final Selector selector = new SimpleSelector(subject, inputMetadata.getOntologyPathPredicate(), (RDFNode) null);
            search(inputMetadata, selector, nodeFactory, treeRoot);
            LOGGER.info("Done searching");
            progressListener.onSearchDone();
        } else {
            LOGGER.info("Invalid query '{}' - no results were found.", query);
            progressListener.onInvalidQuery(query);
        }
        requesting = false;
        return null;
    }

    /**
     * @return {@code true} if a statement was found with the given subject (aka the query), {@code false} otherwise
     */
    private boolean initialSearch(final QueryData inputMetadata) {
        final Selector selector = PREDICATE_FILTER_SELECTOR.apply(inputMetadata.getSubject());
        final Model model = inputMetadata.getModel();

        final StmtIterator stmtIterator = model.listStatements(selector);
        if (!stmtIterator.hasNext()) {
            return false;
        }
        inputMetadata.setCandidateOntologyPathPredicates(stmtIterator);

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
     * @param selector    the selector
     * @param nodeFactory the data node factory
     * @param treeRoot    the tree root
     */
    @SuppressWarnings("unchecked")
    private void search(final QueryData inputMetadata, final Selector selector, final DataNodeFactory<T> nodeFactory, final TreeItem<DataNode<T>> treeRoot) {
        final Model model = inputMetadata.getModel();

        final DataNode<T> curr = nodeFactory.newNode((T) selector.getSubject());
        final TreeItem<DataNode<T>> currTreeItem = progressListener.onCreateNewDataNode(curr, treeRoot);

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

            final DataNode<T> nextNode = nodeFactory.newNode(next);
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
            searchFurther(inputMetadata, nodeFactory, first, treeRoot);
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

        // WARN: Deleted handling for multiple references as it might not even be in the final version.
        progressListener.onAddMultipleDataNodes(currTreeItem, foundData, ref.get());
        searchFurther(inputMetadata, nodeFactory, ref.get(), treeRoot);
    }

    private void searchFurther(final QueryData inputMetadata, final DataNodeFactory<T> nodeFactory, final DataNode<T> next, final TreeItem<DataNode<T>> treeRoot) {
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
        final Model model = inputMetadata.getModel();
        final Resource resource = (Resource) data;
        readResource(model, resource);
        final boolean hasBeenVisited = !usedURIs.add(resource.getURI());
        if (!hasBeenVisited) {
            final Selector sel = new SimpleSelector(resource, inputMetadata.getOntologyPathPredicate(), (RDFNode) null);
            search(inputMetadata, sel, nodeFactory, treeRoot);
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

        final Model model = inputMetadata.getModel();
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
