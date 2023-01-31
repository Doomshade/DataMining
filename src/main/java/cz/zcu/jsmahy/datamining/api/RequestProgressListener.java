package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.dbpedia.DBPediaEndpointTask;
import org.apache.jena.rdf.model.Property;

import java.util.List;

/**
 * <p>A listener for request callbacks. For example when a {@link DataNode} is created the front end would want to handle that fact and update the UI.</p>
 * <p>NOTE: The methods are not necessarily called on the FX UI thread! Make sure to update UI on the UI thread via {@link javafx.application.Platform#runLater(Runnable)}.</p>
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface RequestProgressListener<T> {
    /**
     * Called when the ontology path predicate is set. This callback should highlight the current ontology path predicate in the UI somewhere.
     *
     * @param ontologyPathPredicate the ontology path predicate
     */
    void onSetOntologyPathPredicate(Property ontologyPathPredicate);

    /**
     * Called when a new data node is created. This callback should create a new tree item under the tree root with the given data and return the tree new tree item.
     *
     * @param dataNode the new data node
     * @param parent   the corresponding tree root for the data node
     */
    void onAddNewDataNode(DataNode dataNode, DataNodeRoot parent);

    /**
     * <p>Called when multiple {@link DataNode}s were found and they can be added under a tree item. The {@code treeItem} is not the tree root!</p>
     * <p>An example scenario could be {@code Charles IV, Holy Roman Emperor} having multiple successors (successors from different dynasties) and one would like to see the options under a tree
     * item -- what the user was able to choose from the list and what was chosen -- that can be used for highlighting for example. The chosen data node is guaranteed to be in the data nodes list, and
     * the data nodes list is guaranteed to have size &gt;= 2</p>
     * <p>NOTE: This is optional to handle, but it adds clarity to the building progress.</p>
     *
     * @param dataNodesParent the parent node of the data nodes
     * @param dataNodes       the data nodes
     * @param chosenDataNode  the data node that the user chose to continue under
     */
    void onAddMultipleDataNodes(DataNode dataNodesParent, List<DataNode> dataNodes, DataNode chosenDataNode);

    /**
     * Called when an invalid query is passed. The cause is usually the resource not existing.
     *
     * @param query  the query
     * @param result the initial search result
     */
    void onInvalidQuery(String query, final DBPediaEndpointTask.InitialSearchResult result);

    /**
     * Called when the search is finished.
     */
    void onSearchDone();

    /**
     * Called once the start and end date properties are set. These predicates should be highlighted in the UI somewhere.
     *
     * @param startDateProperty the start date property
     * @param endDateProperty   the end date property
     */
    void setStartAndDateProperty(Property startDateProperty, Property endDateProperty);
}
