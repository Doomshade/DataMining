package cz.zcu.jsmahy.datamining.api;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.web.WebView;
import org.apache.jena.rdf.model.Property;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * <p>A listener for request callbacks. For example when a {@link DataNode} is created the front end would want to handle that fact and update the UI.</p>
 * <p>IMPORTANT: The methods are not necessarily called on the FX UI thread! Make sure to update UI on the UI thread via {@link javafx.application.Platform#runLater(Runnable)}. Also note that if you
 * want to be 100% thread safe do make the methods {@code synchronized}.</p>
 * <p>This class is designed to handle most of the UI related changes via several callbacks. Nothing should be updated on the UI prior to the callback, however it's not guaranteed</p>
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface RequestProgressListener {

    /**
     * @return The property for ontology path predicate.
     */
    ObjectProperty<Property> ontologyPathPredicateProperty();

    /**
     * @return The start date property.
     */
    ObjectProperty<Property> startDateProperty();

    /**
     * @return The end date property.
     */
    ObjectProperty<Property> endDateProperty();

    /**
     * @return The tree root property.
     */
    ObjectProperty<TreeItem<DataNode>> treeRootProperty();

    /**
     * <p>You may use any of the members of the {@link QueryData} instance, and you may also modify them with caution.</p>
     *
     * @return the query data
     */
    ObjectProperty<QueryData> queryDataProperty();

    /**
     * Called when relationships are required to be added to the data nodes. This will likely be called prior to {@link #onAddNewDataNodes(List)}, but is not required to.
     *
     * @param previousDataNode the previous data node
     * @param currentDataNode  the current data node
     */
    void onAddRelationship(DataNode previousDataNode, DataNode currentDataNode);

    /**
     * Called when a new data node is created. This callback should create a new tree item under the tree root with the given data and return the tree new tree item.
     *
     * @param dataNodes the new data node
     */
    void onAddNewDataNodes(List<DataNode> dataNodes);

    /**
     * <p>Called when a data node is deleted. This callback should remove a tree item under the tree root, and cleanup any metadata, such as relationships, pointing to this node.</p>
     * <p>NOTE: the data nodes are likely passed from the selection model of the TreeView, make sure no concurrent changes are made!</p>
     *
     * @param deletedNodes the item that's about to be deleted
     */
    void onDeleteDataNodes(final Collection<DataNode> deletedNodes);

    /**
     * <p>Called when multiple {@link DataNode}s were found and they can be added under a tree item. The {@code treeItem} is not the tree root!</p>
     * <p>An example scenario could be {@code Charles IV, Holy Roman Emperor} having multiple successors (successors from different dynasties) and one would like to see the options under a tree
     * item -- what the user was able to choose from the list and what was chosen -- that can be used for highlighting for example. The chosen data node is guaranteed to be in the data nodes list, and
     * the data nodes list is guaranteed to have size &gt;= 2</p>
     * <p>NOTE: This is optional to handle, but it adds clarity to the building progress.</p>
     *
     * @param dataNodesParent the parent node of the data nodes
     * @param dataNodes       the data nodes
     */
    void onAddMultipleDataNodes(DataNode dataNodesParent, List<DataNode> dataNodes);

    /**
     * Called when an invalid query is passed. The cause is usually the resource not existing.
     *
     * @param query  the query
     * @param result the initial search result
     */
    void onInvalidQuery(String query, final InitialSearchResult result);

    /**
     * Called when the search is finished.
     *
     * @param dataNodeRoot the data node root of the search result
     */
    void onSearchDone(DataNode dataNodeRoot);

    /**
     * Called when a new root is created. This data node root is then used in the following search handled by {@link SparqlEndpointTask}.
     *
     * @param newDataNodeRoot the new data node root
     */
    void onCreateNewRoot(DataNode newDataNodeRoot);

    /**
     * Called once the display of the data is requested. Currently, only browser is supported.
     *
     * @param dataNodeRoot              the data node root of the data
     * @param webView                   the web engine to display the data to
     * @param topLevelFrontendDirectory the "frontend" directory
     */
    void onDisplayRequest(DataNode dataNodeRoot, WebView webView, final File topLevelFrontendDirectory);
}
