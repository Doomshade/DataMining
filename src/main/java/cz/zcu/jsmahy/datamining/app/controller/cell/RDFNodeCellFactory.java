package cz.zcu.jsmahy.datamining.app.controller.cell;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.ResourceBundle;

/**
 * <p>Factory for {@link RDFNode} nodes in a {@link ListView}</p>
 * <p>Implementation details from the official site:
 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html">here</a></p>
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class RDFNodeCellFactory extends ListCell<RDFNode> {
    public static final String SPECIAL_CHARACTERS = "_";
    private static final Logger LOGGER = LogManager.getLogger(RDFNodeCellFactory.class);
    private final ListView<RDFNode> rdfList;

    public RDFNodeCellFactory(final ListView<RDFNode> rdfList, final ResourceBundle resources) {
        this.rdfList = rdfList;

        // TODO: context menu for "add/continue line"
        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem editItem = new MenuItem();

        editItem.textProperty()
                .bind(Bindings.format(resources.getString("ontology.prompt.add"), textProperty()));
        editItem.setOnAction(event -> {
            final RDFNode item = getItem();
            // code to edit item...
        });

        final MenuItem deleteItem = new MenuItem();
        deleteItem.textProperty()
                  .bind(Bindings.format(resources.getString("ontology.prompt.delete"), textProperty()));
        deleteItem.setOnAction(event -> rdfList.getItems()
                                               .remove(getItem()));
        contextMenu.getItems()
                   .addAll(editItem, deleteItem);

        emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if (!isNowEmpty) {
                setContextMenu(contextMenu);
            } else {
                setContextMenu(null);
            }
        });
    }

    /**
     * <p>Formats the {@link RDFNode} to be "pretty" on output.</p>
     * <p>This method will strip any domain off the {@link RDFNode} if it's a {@link Resource}. If it's a {@link Literal}, this will
     * simply return {@link Literal#toString()}. If it's neither, it will return {@link RDFNode#toString()}.</p>
     *
     * @param node the node to format. if null, {@code "null"} is returned.
     *
     * @return {@link RDFNode} in a simple {@link String} representation
     */
    public static String formatRDFNode(RDFNode node) {
        if (node == null) {
            return "null";
        }
        final Marker marker = MarkerManager.getMarker("node-type");
        if (node.isLiteral()) {
            String str = node.asLiteral()
                             .toString();
            final int languageIndex = str.lastIndexOf('@');
            if (languageIndex > 0) {
                str = str.substring(0, languageIndex);
            }
            LOGGER.trace(marker, "Literal \"{}\"", str);
            return str;
        }
        if (node.isResource()) {
            final Resource resource = node.asResource();
            final String uri = resource.getURI();
            final int lastPartIndex = uri.lastIndexOf('/') + 1;

            final String localName = uri.substring(lastPartIndex);
            LOGGER.trace(marker, "Resource \"{}\"", localName);
            return localName;
        }

        LOGGER.debug(marker, "RDFNode \"{}\" was neither literal or resource, using default toString method.", node);
        return node.toString();
    }

    /**
     * <p>Formats the {@link RDFNode} for pretty output in the {@link ListView}.</p>
     * <p>{@link RDFNodeCellFactory#formatRDFNode(RDFNode)} preserves special characters such as "_" - this method gets rids of those</p>
     *
     * @param node the node to format. if null, {@code "null"} is returned.
     *
     * @see RDFNodeCellFactory#formatRDFNode(RDFNode)
     */
    private static String prettyFormat(RDFNode node) {
        return formatRDFNode(node).replaceAll(SPECIAL_CHARACTERS, " ");
    }

    @Override
    protected void updateItem(final RDFNode item, final boolean empty) {
        super.updateItem(item, empty);
        setText(item == null ? null : prettyFormat(item));
    }
}
