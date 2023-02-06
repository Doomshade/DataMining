package cz.zcu.jsmahy.datamining.dbpedia;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import cz.zcu.jsmahy.datamining.resolvers.OntologyPathPredicateResolver;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.AbstractDateTime;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.XSDDuration;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;
import static cz.zcu.jsmahy.datamining.resolvers.MultipleItemChoiceResolver.RESULT_KEY_CHOSEN_RDF_NODE;
import static cz.zcu.jsmahy.datamining.resolvers.StartAndEndDateResolver.RESULT_KEY_END_DATE_PREDICATE;
import static cz.zcu.jsmahy.datamining.resolvers.StartAndEndDateResolver.RESULT_KEY_START_DATE_PREDICATE;
import static cz.zcu.jsmahy.datamining.util.RDFNodeUtil.setDataNodeNameFromRDFNode;

/**
 * The DBPedia {@link SparqlEndpointTask}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaEndpointTask<R> extends DefaultSparqlEndpointTask<R> {
    private static final Logger LOGGER = LogManager.getLogger(DBPediaEndpointTask.class);
    /**
     * <p>This comparator ensures the URI resources are placed first over literal resources.</p>
     * <p>The reasoning behind is simple: if the user wishes to fast forward when searching down the line we need to iterate through URI resources first.</p>
     */
    private static final Comparator<Statement> STATEMENT_COMPARATOR = (x, y) -> Boolean.compare(x.getObject()
                                                                                                 .isURIResource(),
                                                                                                y.getObject()
                                                                                                 .isURIResource());
    private static final Property PROPERTY_DBO_ABSTRACT = ResourceFactory.createProperty("https://dbpedia.org/ontology/abstract");
    private final Collection<String> usedURIs = new HashSet<>();

    @Inject
    @SuppressWarnings("rawtypes")
    public DBPediaEndpointTask(final String query,
                               final DataNode dataNodeRoot,
                               final ApplicationConfiguration config,
                               final RequestProgressListener progressListener,
                               final DataNodeFactory dataNodeFactory,
                               final @Named("userAssisted") ResponseResolver ambiguousResultResolver,
                               final @Named("ontologyPathPredicate") ResponseResolver ontologyPathPredicateResolver,
                               final @Named("date") ResponseResolver startAndEndDateResolver) {
        super(query, dataNodeRoot, config, progressListener, dataNodeFactory, ambiguousResultResolver, ontologyPathPredicateResolver, startAndEndDateResolver);
    }

    private void addDatesToNode(final Model model, final DataNode curr, final Property dateProperty, final Resource subject, final boolean isStartDate) {
        final Selector startDateSelector = new SimpleSelector(subject, dateProperty, (Object) null);
        final StmtIterator startDates = model.listStatements(startDateSelector);
        if (startDates.hasNext()) {
            // TODO: account for multiple values
            final AbstractDateTime innerDateType = (AbstractDateTime) startDates.next()
                                                                                .getObject()
                                                                                .asLiteral()
                                                                                .getValue();
            final Calendar calendar;
            if (innerDateType instanceof XSDDateTime dateTime) {
                calendar = dateTime.asCalendar();
            } else if (innerDateType instanceof XSDDuration duration) {
                final long millis = duration.getFullSeconds() * 1000L;
                // default duration to gregorian calendar
                calendar = new GregorianCalendar();
                calendar.setTimeInMillis(millis);
            } else {
                throw new ClassCastException("Inner date type is of unknown value: " + innerDateType);
            }
            LOGGER.debug("Setting {} date (inner type: {}, actual date: {}) to {}", isStartDate ? "start" : "end", innerDateType, calendar, curr.getValue(METADATA_KEY_NAME, "<no name>"));
            if (isStartDate) {
                curr.addMetadata(DataNode.METADATA_KEY_START_DATE, calendar);
            } else {
                curr.addMetadata(DataNode.METADATA_KEY_END_DATE, calendar);
            }
        } else {
            // TODO: solve this
            LOGGER.warn("No {} date found for {}!", isStartDate ? "start" : "end", subject);
            // if the end date was not found, default it to the start date
            if (!isStartDate) {
                curr.getValue(DataNode.METADATA_KEY_START_DATE)
                    .ifPresent(startDate -> {
                        LOGGER.debug("Setting end date to match start date ({}) because no end date was found.", startDate);
                        curr.addMetadata(DataNode.METADATA_KEY_END_DATE, startDate);
                    });
            }
        }
    }

    @Override
    public synchronized R call() throws InvalidQueryException {
        final RequestProgressListener progressListener = config.getProgressListener();
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
            progressListener.queryDataProperty()
                            .set(inputMetadata);
            final Selector selector = new SimpleSelector(subject, inputMetadata.getOntologyPathPredicate(), (RDFNode) null);
            search(inputMetadata, selector, null);
            LOGGER.info("Done searching");
//            dataNodeRoot.iterate(((dataNode, integer) -> System.out.println(dataNode)));
            progressListener.onSearchDone();
        } else {
            LOGGER.info("Invalid createBackgroundSparqlRequest '{}' - no results were found. Initial search result: {}", query, result);
            progressListener.onInvalidQuery(query, result);
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
        LOGGER.debug("Found ontology path predicate.");

        return InitialSearchResult.OK;
    }

    private InitialSearchResult requestStartAndEndDatePredicate(final QueryData inputMetadata) {
        final Selector selector = createSelector(inputMetadata.getInitialSubject(), stmt -> {
            final RDFNode object = stmt.getObject();
            if (!object.isLiteral()) {
                return false;
            }
            // the valid date format corresponds to the date datatype URI
            // for example URL http://somewhere.else/foo_data_bar
            // the data type is foo_data_bar
            // we have two choices:
            // either this could have false positives as the URI could be contained in the URL
            // (or it could simply be as a part of an invalid date type like foo_data_bar_invalid)
            // or false negatives, i.e. the date type data type is not found
            // we heavily rely on Apache Jena's API to be precise in this matter -- it has to have the
            // date types mapped correctly, otherwise this won't work
            // the commented code below is there to showcase the first case where we could have false positives
            // we chose option #2 as it's way less likely to encounter a false negative
            // these checks can definitely be improved, but so far we will leave it as is
            // by checking the URI
            final RDFDatatype dataType = object.asNode()
                                               .getLiteralDatatype();
            final String validDateFormatUri = dataType.getURI();
            for (String validDateFormat : validDateFormats) {
                if (validDateFormatUri.contains(validDateFormat)) {
                    return true;
                }
            }
            return validDateFormats.contains(validDateFormatUri.substring(validDateFormatUri.lastIndexOf('/')));
        });
        final Model model = inputMetadata.getCurrentModel();
        final StmtIterator statements = model.listStatements(selector);
        if (!statements.hasNext()) {
            LOGGER.info("No date found for input {}", inputMetadata);
            return InitialSearchResult.NO_DATE_FOUND;
        }

        startAndEndDateResolver.resolve(Collections.unmodifiableCollection(statements.toList()), this);
        while (!startAndEndDateResolver.hasResponseReady()) {
            try {
                wait(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        final ArbitraryDataHolder ref = startAndEndDateResolver.getResponse();
        if (ref == null) {
            LOGGER.error("Internal error occurred when resolving request for date input. Selector: {}", selector);
            return InitialSearchResult.UNKNOWN;
        }

        final Optional<Property> startDatePropertyOpt = ref.getValue(RESULT_KEY_START_DATE_PREDICATE);
        final Property endDateProperty = ref.getValue(RESULT_KEY_END_DATE_PREDICATE, null);

        // TODO: this should be handled somewhere else
        if (startDatePropertyOpt.isEmpty()) {
            return InitialSearchResult.START_DATE_NOT_SELECTED;
        }

        inputMetadata.setStartDateProperty(startDatePropertyOpt.get());
        // the end date does need to be specified
        inputMetadata.setEndDateProperty(endDateProperty);
        progressListener.startDateProperty()
                        .set(startDatePropertyOpt.get());
        progressListener.endDateProperty()
                        .set(endDateProperty);
        return InitialSearchResult.OK;
    }

    private InitialSearchResult requestOntologyPathPredicate(final QueryData inputMetadata) {
        final Selector selector = createSelector(inputMetadata.getInitialSubject(), stmt -> {
            if (!stmt.getObject()
                     .isURIResource()) {
                return false;
            }
            final Property predicate = stmt.getPredicate();
            for (String ignoredPathPredicate : ignoredPathPredicates) {
                if (predicate.hasURI(ignoredPathPredicate)) {
                    return false;
                }
            }
            return true;
        });
        final Model model = inputMetadata.getCurrentModel();

        final StmtIterator statements = model.listStatements(selector);
        if (!statements.hasNext()) {
            return InitialSearchResult.SUBJECT_NOT_FOUND;
        }
        ontologyPathPredicateResolver.resolve(Collections.unmodifiableList(statements.toList()), this);
        while (!ontologyPathPredicateResolver.hasResponseReady()) {
            try {
                wait(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        final ArbitraryDataHolder ref = ontologyPathPredicateResolver.getResponse();
        if (ref == null) {
            return InitialSearchResult.UNKNOWN;
        }
        final Optional<Property> ontologyPathPredicateOpt = ref.getValue(OntologyPathPredicateResolver.RESULT_KEY_ONTOLOGY_PATH_PREDICATE);
        if (ontologyPathPredicateOpt.isEmpty()) {
            return InitialSearchResult.PATH_NOT_SELECTED;
        }

        inputMetadata.setOntologyPathPredicate(ontologyPathPredicateOpt.get());
        config.getProgressListener()
              .ontologyPathPredicateProperty()
              .set(ontologyPathPredicateOpt.get());
        return InitialSearchResult.OK;
    }

    private Selector createSelector(final Resource initialSubject, final Predicate<Statement> selectorSelects) {
        return new SimpleSelector(initialSubject, null, null, "en") {
            @Override
            public boolean selects(final Statement s) {
                return selectorSelects.test(s);
            }
        };
    }

    private void initializeDataNode(final DataNode dataNode, final RDFNode node, final QueryData inputMetadata) {
        dataNode.addMetadata(DataNode.METADATA_KEY_RDF_NODE, node);
        setDataNodeNameFromRDFNode(dataNode, node);
        if (node instanceof Resource resource) {
            // TODO: this does not work
            final Statement dboAbstract = resource.getProperty(PROPERTY_DBO_ABSTRACT);
            if (dboAbstract != null) {
                dataNode.addMetadata(DataNode.METADATA_KEY_DESCRIPTION,
                                     dboAbstract.getSubject()
                                                .getLocalName());
            }
            addDatesToNode(inputMetadata.getCurrentModel(), dataNode, inputMetadata.getStartDateProperty(), resource, true);
            addDatesToNode(inputMetadata.getCurrentModel(), dataNode, inputMetadata.getEndDateProperty(), resource, false);
        }
    }

    /**
     * Recursively searches based on the given model, selector, and a previous link. Adds the subject of the selector to the tree.
     *
     * @param selector the selector
     */
    private void search(final QueryData inputMetadata, final Selector selector, final DataNode prev) {
        LOGGER.debug("Search");
        final Model model = inputMetadata.getCurrentModel();
        final RequestProgressListener progressListener = config.getProgressListener();

        final DataNodeFactory nodeFactory = config.getDataNodeFactory();
        final DataNode curr = nodeFactory.newNode(dataNodeRoot);
        final Resource currRDFNode = selector.getSubject();
        initializeDataNode(curr, currRDFNode, inputMetadata);

        progressListener.onAddNewDataNode(dataNodeRoot, prev, curr);

        final List<Statement> statements = model.listStatements(selector)
                                                .toList();
        statements.sort(STATEMENT_COMPARATOR);

        final List<RDFNode> foundDataList = new ArrayList<>();
        RDFNode previous = null;
        for (final Statement stmt : statements) {
            final RDFNode next = stmt.getObject();
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

            LOGGER.debug("Found {}", next);
            foundDataList.add(next);
        }

        // no nodes found, stop searching
        if (foundDataList.isEmpty()) {
            return;
        }

        // only one found, that means it's going linearly
        // we can continue searching
        if (foundDataList.size() == 1) {
            final RDFNode first = foundDataList.get(0);
            searchFurther(inputMetadata, first, curr);
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
        LOGGER.debug("Found multiple nodes, asking user to clarify...");
        ambiguousResultResolver.resolve(foundDataList, this);
        while (!ambiguousResultResolver.hasResponseReady()) {
            try {
                wait(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        final ArbitraryDataHolder response = ambiguousResultResolver.getResponse();
        final Optional<RDFNode> chosenNextRDFNodeOpt = response.getValue(RESULT_KEY_CHOSEN_RDF_NODE);
        if (chosenNextRDFNodeOpt.isEmpty()) {
            LOGGER.debug("Received no response");
            return;
        }
        final RDFNode chosenNextRDFNode = chosenNextRDFNodeOpt.get();
        LOGGER.debug("Received next node: {}", chosenNextRDFNode);

        final List<DataNode> currDataNodeChildren = new ArrayList<>();
        // WARN: Deleted handling for multiple references as it might not even be in the final version.
        for (final RDFNode rdfNode : foundDataList) {
            final DataNode child = nodeFactory.newNode(curr);
            child.addMetadata(DataNode.METADATA_KEY_RDF_NODE, rdfNode);
            setDataNodeNameFromRDFNode(child, rdfNode);
            currDataNodeChildren.add(child);
        }
        progressListener.onAddMultipleDataNodes(curr, currDataNodeChildren, chosenNextRDFNode);
        searchFurther(inputMetadata, chosenNextRDFNode, curr);
    }

    private void searchFurther(final QueryData inputMetadata, final RDFNode next, final DataNode curr) {
        // attempts to search further down the line if the given data is a URI resource
        // if it's not a URI resource the searching terminates
        // if it's a URI resource we first check if we've been here already - we don't want to be stuck in a cycle,
        // that's what usedURIs is for
        // otherwise build a selector and continue searching down the line
        if (next == null) {
            return;
        }
        if (!next.isURIResource()) {
            return;
        }
        final Model model = inputMetadata.getCurrentModel();
        final Resource resource = (Resource) next;
        readResource(model, resource);
        final boolean hasBeenVisited = !usedURIs.add(resource.getURI());
        if (!hasBeenVisited) {
            final Selector sel = new SimpleSelector(resource, inputMetadata.getOntologyPathPredicate(), (RDFNode) null);
            search(inputMetadata, sel, curr);
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
    private boolean meetsRequirements(final QueryData inputMetadata, final RDFNode previous, final RDFNode curr) throws IllegalArgumentException {
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
