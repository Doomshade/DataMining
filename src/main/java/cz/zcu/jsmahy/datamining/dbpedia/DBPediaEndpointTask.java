package cz.zcu.jsmahy.datamining.dbpedia;

import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The DBPedia {@link SparqlEndpointTask}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaEndpointTask<T extends RDFNode, R extends Void> extends SparqlEndpointTask<T, R, DBPediaApplicationConfiguration<T, R>> {
    private static final Logger LOGGER = LogManager.getLogger(DBPediaEndpointTask.class);
    /**
     * <p>This comparator ensures the URI resources are placed first over literal resources.</p>
     * <p>The reasoning behind is simple: if the user wishes to fast forward when searching down the line we need to iterate through URI resources first.</p>
     */
    private static final Comparator<Statement> STATEMENT_COMPARATOR = (x, y) -> Boolean.compare(x.getObject()
                                                                                                 .isURIResource(),
                                                                                                y.getObject()
                                                                                                 .isURIResource());


    private final Collection<String> ignoredPathPredicates = new HashSet<>();
    private final Collection<String> validDateFormats = new HashSet<>();
    //</editor-fold>

    private final Collection<String> usedURIs = new HashSet<>();


    public DBPediaEndpointTask(final DBPediaApplicationConfiguration<T, R> config, final DataNodeFactory<T> nodeFactory, final String query, final DataNodeRoot<T> dataNodeRoot) {
        // DBPEDIA_BASE_URL
        super(config, nodeFactory, query, dataNodeRoot);
        final List<String> ignoredPathPredicates = config.getIgnoredPathPredicates();
        this.ignoredPathPredicates.addAll(ignoredPathPredicates);
        final Set<String> validDateFormats = config.getValidDateFormats()
                                                   .stream()
                                                   .map(String::toLowerCase)
                                                   .collect(Collectors.toSet());
        if (validDateFormats.contains("any")) {
            this.validDateFormats.addAll(DBPediaApplicationConfiguration.ALL_VALID_DATE_FORMATS);
        } else {
            this.validDateFormats.addAll(validDateFormats);
        }
    }

    @Override
    protected synchronized R call() throws InvalidQueryException {
        final RequestProgressListener<T> progressListener = config.getProgressListener();
        final Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        final QueryData inputMetadata = new QueryData();
        try {
            LOGGER.info("Requesting {} for initial information.", query);
            model.read(query);
            inputMetadata.setCurrentModel(model);
        } catch (HttpException e) {
            throw LOGGER.throwing(e);
        } catch (Exception e) {
            throw new InvalidQueryException(e);
        }

        final Resource subject = model.getResource(query);
        inputMetadata.setInitialSubject(subject);
        inputMetadata.setRestrictions(new ArrayList<>());
        this.usedURIs.clear();

        LOGGER.info("Start searching");
        final InitialSearchResult result = initialSearch(inputMetadata);
        if (result == InitialSearchResult.OK) {
            final Selector selector = new SimpleSelector(subject, inputMetadata.getOntologyPathPredicate(), (RDFNode) null);
            search(inputMetadata, selector);
            LOGGER.info("Done searching");
            dataNodeRoot.iterate(((dataNode, integer) -> System.out.println(dataNode)));
            progressListener.onSearchDone();
        } else {
            LOGGER.info("Invalid createBackgroundSparqlRequest '{}' - no results were found. Initial search result: {}", this.query, result);
            progressListener.onInvalidQuery(this.query, result);
        }
        return null;
    }

    /**
     * @return the result of the initial search
     *
     * @see InitialSearchResult
     */
    private InitialSearchResult initialSearch(final QueryData inputMetadata) {
        LOGGER.debug("Initiating search on subject {}", inputMetadata.getInitialSubject());
        final InitialSearchResult startAndEndDateResult = requestStartAndEndDatePredicate(inputMetadata);
        if (startAndEndDateResult != InitialSearchResult.OK) {
            return startAndEndDateResult;
        }
        LOGGER.debug("Found start and end date.");

        final InitialSearchResult pathPredicateResult = requestOntologyPathPredicate(inputMetadata);
        if (pathPredicateResult != InitialSearchResult.OK) {
            return pathPredicateResult;
        }
        LOGGER.debug("Found onotology path predicate.");

        return InitialSearchResult.OK;
    }

    private InitialSearchResult requestStartAndEndDatePredicate(final QueryData inputMetadata) {
        final Selector selector = createSelector(inputMetadata, stmt -> {
            final RDFNode object = stmt.getObject();
            if (!object.isLiteral()) {
                return false;
            }
            final RDFDatatype dataType = object.asNode()
                                               .getLiteralDatatype();
            final String uri = dataType.getURI();
            for (String s : validDateFormats) {
                if (uri.contains(s)) {
                    return true;
                }
            }
            return validDateFormats.contains(uri.substring(uri.lastIndexOf('/')));
        });
        final Model model = inputMetadata.getCurrentModel();
        final StmtIterator stmtIterator = model.listStatements(selector);
        if (!stmtIterator.hasNext()) {
            LOGGER.info("No date found for input {}", inputMetadata);
            return InitialSearchResult.NO_DATE_FOUND;
        }

        final List<Statement> statements = stmtIterator.toList();
        inputMetadata.setCandidatesForStartAndEndDates(statements);

        final DataNodeReferenceHolder<T> ref = config.getStartAndEndDateResolver()
                                                     .resolveRequest(null, inputMetadata, this);
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
            LOGGER.error("Internal error occurred when resolving request for date input. Selector: {}", selector);
            return InitialSearchResult.UNKNOWN;
        }
        final Property startDateProperty = ref.getStartDatePredicate();
        final Property endDateProperty = ref.getEndDatePredicate();

        if (startDateProperty == null) {
            return InitialSearchResult.START_DATE_NOT_SELECTED;
        }
        if (endDateProperty == null) {
            return InitialSearchResult.END_DATE_NOT_SELECTED;
        }

        inputMetadata.setStartDateProperty(startDateProperty);
        inputMetadata.setEndDateProperty(endDateProperty);
        config.getProgressListener()
              .setStartAndDateProperty(startDateProperty, endDateProperty);
        return InitialSearchResult.OK;
    }

    private InitialSearchResult requestOntologyPathPredicate(final QueryData inputMetadata) {
        final Selector selector = createSelector(inputMetadata, stmt -> {
            final Property predicate = stmt.getPredicate();
            for (String ignoredPathPredicate : ignoredPathPredicates) {
                if (predicate.hasURI(ignoredPathPredicate)) {
                    return false;
                }
            }
            return true;
        });
        final Model model = inputMetadata.getCurrentModel();

        final StmtIterator stmtIterator = model.listStatements(selector);
        if (!stmtIterator.hasNext()) {
            return InitialSearchResult.SUBJECT_NOT_FOUND;
        }
        inputMetadata.setCandidatesForOntologyPathPredicate(Collections.unmodifiableList(stmtIterator.toList()));

        final DataNodeReferenceHolder<T> ref = config.getOntologyPathPredicateResolver()
                                                     .resolveRequest(null, inputMetadata, this);
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
            return InitialSearchResult.UNKNOWN;
        }
        final Property ontologyPathPredicate = ref.getOntologyPathPredicate();
        if (ontologyPathPredicate == null) {
            return InitialSearchResult.PATH_NOT_SELECTED;
        }

        inputMetadata.setOntologyPathPredicate(ontologyPathPredicate);
        config.getProgressListener()
              .onSetOntologyPathPredicate(ontologyPathPredicate);
        return InitialSearchResult.OK;
    }

    private Selector createSelector(final QueryData inputMetadata, final Predicate<Statement> selectorSelects) {
        return new SimpleSelector(inputMetadata.getInitialSubject(), null, null, "en") {
            @Override
            public boolean selects(final Statement s) {
                return selectorSelects.test(s);
            }
        };
    }

    public enum InitialSearchResult {
        OK,
        SUBJECT_NOT_FOUND,
        PATH_NOT_SELECTED,
        NO_DATE_FOUND,
        START_DATE_NOT_SELECTED,
        END_DATE_NOT_SELECTED,
        UNKNOWN
    }


    /**
     * Recursively searches based on the given model, selector, and a previous link. Adds the subject of the selector to the tree.
     *
     * @param selector the selector
     */
    @SuppressWarnings("unchecked")
    private void search(final QueryData inputMetadata, final Selector selector) {
        final Model model = inputMetadata.getCurrentModel();

        final DataNodeFactory<T> nodeFactory = config.getNodeFactory();
        final DataNode<T> curr = nodeFactory.newNode((T) selector.getSubject(), dataNodeRoot);
        final RequestProgressListener<T> progressListener = config.getProgressListener();
        progressListener.onAddNewDataNode(curr, dataNodeRoot);

        final List<Statement> statements = model.listStatements(selector)
                                                .toList();
        statements.sort(STATEMENT_COMPARATOR);

        final List<DataNode<T>> foundDataList = new ArrayList<>();
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

            final DataNode<T> nextNode = nodeFactory.newNode(next, dataNodeRoot);
            LOGGER.debug("Found {}", next);
            foundDataList.add(nextNode);
        }

        // no nodes found, stop searching
        if (foundDataList.isEmpty()) {
            return;
        }

        // only one found, that means it's going linearly
        // we can continue searching
        if (foundDataList.size() == 1) {
            final DataNode<T> first = foundDataList.get(0);
            searchFurther(inputMetadata, first);
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
        final DataNodeReferenceHolder<T> ref = config.getAmbiguousResultResolver()
                                                     .resolveRequest(foundDataList, inputMetadata, this);
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

        final List<DataNode<T>> currDataNodeChildren = new ArrayList<>();
        // WARN: Deleted handling for multiple references as it might not even be in the final version.
        for (final DataNode<T> foundData : foundDataList) {
            currDataNodeChildren.add(nodeFactory.newNode(foundData.getData(), curr));
        }
        progressListener.onAddMultipleDataNodes(curr, currDataNodeChildren, chosenDataNode);
        searchFurther(inputMetadata, chosenDataNode);
    }

    private void searchFurther(final QueryData inputMetadata, final DataNode<T> next) {
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
            search(inputMetadata, sel);
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
