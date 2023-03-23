package cz.zcu.jsmahy.datamining.dbpedia;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.resolvers.OntologyPathPredicateResolver;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.XSDDuration;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;

import static cz.zcu.jsmahy.datamining.api.Alerts.alertConnectionProblems;
import static cz.zcu.jsmahy.datamining.api.DataNode.*;
import static cz.zcu.jsmahy.datamining.resolvers.MultipleItemChoiceResolver.RESULT_KEY_CHOSEN_RDF_NODE;
import static cz.zcu.jsmahy.datamining.resolvers.StartAndEndDateResolver.RESULT_KEY_END_DATE_PREDICATE;
import static cz.zcu.jsmahy.datamining.resolvers.StartAndEndDateResolver.RESULT_KEY_START_DATE_PREDICATE;
import static cz.zcu.jsmahy.datamining.util.RDFNodeUtil.setDataNodeNameFromRDFNode;
import static java.util.Objects.requireNonNull;

/**
 * The DBPedia {@link SparqlEndpointTask}.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DBPediaEndpointTask<R> extends DefaultSparqlEndpointTask<R> {
    public static final int MAX_REDIRECTS = 20;
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
    private static final Property PROPERTY_REDIRECT = ResourceFactory.createProperty("http://dbpedia.org/ontology/wikiPageRedirects");
    private final DataNodeFactory dataNodeFactory;
    private final ResponseResolver<Collection<Statement>> ambiguousResultResolver;
    private final ResponseResolver<Collection<Statement>> ontologyPathPredicateResolver;
    private final ResponseResolver<Collection<Statement>> startAndEndDateResolver;
    private final Collection<String> usedURIs = new HashSet<>();

    @Inject
    @SuppressWarnings("unchecked, rawtypes")
    public DBPediaEndpointTask(final String query,
                               final DataNode dataNodeRoot,
                               final ApplicationConfiguration config,
                               final RequestProgressListener progressListener,
                               final DataNodeFactory dataNodeFactory,
                               final @Named("userAssisted") ResponseResolver ambiguousResultResolver,
                               final @Named("ontologyPathPredicate") ResponseResolver ontologyPathPredicateResolver,
                               final @Named("date") ResponseResolver startAndEndDateResolver) {
        super(query, dataNodeRoot, config, progressListener);
        this.dataNodeFactory = requireNonNull(dataNodeFactory);
        this.ambiguousResultResolver = requireNonNull(ambiguousResultResolver);
        this.ontologyPathPredicateResolver = requireNonNull(ontologyPathPredicateResolver);
        this.startAndEndDateResolver = requireNonNull(startAndEndDateResolver);
    }

    private void addDatesToNode(final Model model, final DataNode curr, final Property dateProperty, final Resource subject, final boolean isStartDate) {
        if (dateProperty == null) {
            LOGGER.trace("No date property set, not adding dates to {}", curr);
            return;
        }

        final Selector startDateSelector = new SimpleSelector(subject, dateProperty, (Object) null);
        final StmtIterator startDates = model.listStatements(startDateSelector);
        if (startDates.hasNext()) {
            final RDFNode object = startDates.next()
                                             .getObject();
            if (!object.isLiteral()) {
                // TODO: handle this
                LOGGER.error("The {} date property is a URI. This should be a literal! S: {}, P: {}, O: {}", isStartDate ? "start" : "end", subject, dateProperty, object);
                return;
            }
            final Object value = object.asLiteral()
                                       .getValue();
            final Calendar calendar;
            if (value instanceof XSDDateTime dateTime) {
                calendar = dateTime.asCalendar();
            } else if (value instanceof XSDDuration duration) {
                final long millis = duration.getFullSeconds() * 1000L;
                calendar = new GregorianCalendar();
                calendar.setTimeInMillis(millis);
            } else if (value instanceof Integer integer) {
                final long millis = integer * 1000L;
                calendar = new GregorianCalendar();
                calendar.setTimeInMillis(millis);
            } else {
                throw new ClassCastException("Inner date type is of unknown value: " + value);
            }
            LOGGER.trace("Setting {} date (inner type: {}, actual date: {}) to {}", isStartDate ? "start" : "end", value, calendar, curr.getValue(METADATA_KEY_NAME, "<no name>"));
            if (isStartDate) {
                curr.addMetadata(METADATA_KEY_START_DATE, calendar);
            } else {
                curr.addMetadata(METADATA_KEY_END_DATE, calendar);
            }
        } else {
            if (isStartDate) {
                LOGGER.error("No start date {} found for {}!", dateProperty, subject);
            } else {
                LOGGER.debug("No end date {} found for {}. This will be considered a 'moment'", dateProperty, subject);
            }
        }
    }

    @Override
    public synchronized R call() {
        this.usedURIs.clear();
        final Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        final QueryData inputMetadata = new QueryData();
        try {
            LOGGER.info("Requesting {} for initial information.", query);
            long start = System.currentTimeMillis();
            LOGGER.trace("Querying {}", query);
            model.read(query);
            long end = System.currentTimeMillis() - start;
            LOGGER.trace("Querying {} took {}ms", query, end);
            inputMetadata.setCurrentModel(model);
        } catch (HttpException e) {
            alertConnectionProblems(e);
            throw LOGGER.throwing(e);
        }

        final InitialSearchResult result;
        final boolean subjectFound = model.listStatements()
                                          .hasNext();
        if (!subjectFound) {
            result = InitialSearchResult.SUBJECT_NOT_FOUND;
        } else {
            // get the initial data such as start date, end date etc
            final Resource subject = model.createResource(query);
            inputMetadata.setInitialSubject(redirectIfPossible(subject, model));
            inputMetadata.setRestrictions(new ArrayList<>());
            result = initialSearch(inputMetadata);
        }
        if (result != InitialSearchResult.OK) {
            LOGGER.info("Search result of '{}' was not {}. Initial search result: {}", query, InitialSearchResult.OK, result);
            progressListener.onInvalidQuery(originalQuery, result);
            return null;
        }

        progressListener.queryDataProperty()
                        .set(inputMetadata);
        final Selector selector = new SimpleSelector(inputMetadata.getInitialSubject(), inputMetadata.getOntologyPathPredicate(), (RDFNode) null);
        LOGGER.info("Start searching");
        search(inputMetadata, selector, null);
        LOGGER.info("Done searching");
//            dataNodeRoot.iterate(((dataNode, integer) -> System.out.println(dataNode)));
        progressListener.onSearchDone(dataNodeRoot);

        return null;

    }

    private Resource redirectIfPossible(final Resource subject, final Model model) {
        return redirectIfPossible(subject, model, MAX_REDIRECTS);
    }

    private Resource redirectIfPossible(final Resource subject, final Model model, final int maxRedirects) {
        if (maxRedirects <= 0 || maxRedirects > MAX_REDIRECTS) {
            return subject;
        }
        // DBPEDIA SPECIFIC
        final StmtIterator stmts = model.listStatements(subject, PROPERTY_REDIRECT, (RDFNode) null);
        if (!stmts.hasNext()) {
            LOGGER.trace("No redirects found for {}.", subject);
            return subject;
        }
        final RDFNode object = stmts.next()
                                    .getObject();
        if (!object.isURIResource()) {
            LOGGER.debug("Found a redirect {}, but it's not a URI resource.", object);
            return subject;
        }
        // continue redirecting
        final Resource newSubject = object.asResource();
        LOGGER.debug("Redirecting to {}.", newSubject);
        model.read(newSubject.getURI());
        return redirectIfPossible(newSubject, model, maxRedirects - 1);
    }

    /**
     * @return the result of the initial search
     *
     * @see InitialSearchResult
     */
    private InitialSearchResult initialSearch(final QueryData inputMetadata) {
        LOGGER.debug("Initiating search on subject {}", inputMetadata.getInitialSubject());

        final InitialSearchResult pathPredicateResult = requestOntologyPathPredicate(inputMetadata);
        if (pathPredicateResult != InitialSearchResult.OK) {
            return pathPredicateResult;
        }
        LOGGER.debug("Found ontology path predicate.");

        final InitialSearchResult startAndEndDateResult = requestStartAndEndDatePredicate(inputMetadata);
        if (startAndEndDateResult != InitialSearchResult.OK) {
            return startAndEndDateResult;
        }
        LOGGER.debug("Found start and end date.");

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
            return InitialSearchResult.SUBJECT_NOT_FOUND;
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
        progressListener.ontologyPathPredicateProperty()
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
        dataNode.addMetadata(METADATA_KEY_RDF_NODE, node);
        setDataNodeNameFromRDFNode(dataNode, node);
        if (node instanceof Resource resource) {
            // TODO: this does not work
            final Statement dboAbstract = resource.getProperty(PROPERTY_DBO_ABSTRACT);
            if (dboAbstract != null) {
                dataNode.addMetadata(METADATA_KEY_DESCRIPTION,
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
        // create a new node and add it to the model
        final Model model = inputMetadata.getCurrentModel();
        final DataNode curr = dataNodeFactory.newNode(dataNodeRoot);
        final Resource subject = selector.getSubject();
        initializeDataNode(curr, subject, inputMetadata);
        progressListener.onAddRelationship(prev, curr);
        progressListener.onAddNewDataNodes(List.of(curr));

        // list possible child nodes
        final List<Statement> statements = model.listStatements(selector)
                                                .toList();
        statements.sort(STATEMENT_COMPARATOR);
        final List<Statement> foundDataList = new ArrayList<>();
        // for each child: read the child into the model if it's a URI
        // redirect if possible
        // check for requirements of the child
        // if the requirements are ok, continue to the next child
        for (Statement stmt : statements) {
            RDFNode object = stmt.getObject();
            if (object.isURIResource()) {
                // Log the query time of the object
                LOGGER.trace("Object is a URI resource, querying the resource");
                long start = System.currentTimeMillis();
                final String uri = object.asResource()
                                         .getURI();
                LOGGER.trace("Querying {}", uri);
                model.read(uri);
                long end = System.currentTimeMillis() - start;
                LOGGER.trace("Querying {} took {}ms", uri, end);

                // Log the redirect time of the object
                final RDFNode priorToRedirect = object;
                start = System.currentTimeMillis();
                object = redirectIfPossible(object.asResource(), model);
                end = System.currentTimeMillis() - start;

                // If the objects do not equal we redirected, log that too
                if (priorToRedirect != object) {
                    final String actualUri = object.isURIResource() ?
                                             " to " + object.asResource()
                                                            .getURI() :
                                             "";
                    LOGGER.trace("Redirecting{} took {}ms", actualUri, end);
                }

                // Update the statement because we (possibly) received a new object
                // TODO: This could be in the "if" above, but I don't wanna touch this code just in case
                stmt = model.createStatement(stmt.getSubject(), stmt.getPredicate(), object);
            }

            if (!meetsRequirements(inputMetadata, object)) {
                continue;
            }

            LOGGER.debug("Found {}", object);
            foundDataList.add(stmt);
        }

        // no nodes found, stop searching
        if (foundDataList.isEmpty()) {
            return;
        }

        // only one found, that means it's going linearly
        // we can continue searching
        if (foundDataList.size() == 1) {
            final Statement first = foundDataList.get(0);
            searchFurther(inputMetadata, first.getObject(), curr);
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
        LOGGER.debug("User chosen node: {}", chosenNextRDFNode);

        final List<DataNode> currDataNodeChildren = new ArrayList<>();
        // WARN: Deleted handling for multiple references as it might not even be in the final version.
        for (final Statement stmt : foundDataList) {
            final RDFNode object = stmt.getObject();
            final DataNode child = dataNodeFactory.newNode(curr);
            child.addMetadata(METADATA_KEY_RDF_NODE, object);
            setDataNodeNameFromRDFNode(child, object);
            currDataNodeChildren.add(child);
        }
        progressListener.onAddNewDataNodes(currDataNodeChildren);
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

        long start = System.currentTimeMillis();
        LOGGER.trace("Querying {}", resource.getURI());
        model.read(resource.getURI());
        long end = System.currentTimeMillis() - start;
        LOGGER.trace("Querying {} took {}ms", resource.getURI(), end);

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
     * @param curr the current node
     *
     * @return Whether the current node meets requirements. Read this method's javadoc for more information regarding the changes this method potentially makes to the model.
     *
     * @throws IllegalStateException if the previous is not a URI resource
     */
    private boolean meetsRequirements(final QueryData inputMetadata, final RDFNode curr) throws IllegalArgumentException {
        if (!curr.isURIResource()) {
            return true;
        }

        final Model model = inputMetadata.getCurrentModel();
        final Resource resource = curr.asResource();
        // check for the restrictions on the given request
        // NOTE: this does nothing at the moment! this means this method ALWAYS returns true
        for (final Restriction restriction : inputMetadata.getRestrictions()) {
            final Property predicate = model.getProperty(restriction.getNamespace(), restriction.getLink());
            final Selector sel = new SimpleSelector(resource, predicate, (RDFNode) null);
            final boolean foundSomething = model.listStatements(sel)
                                                .hasNext();
            if (!foundSomething) {
                return false;
            }
        }
        return true;
    }
}
