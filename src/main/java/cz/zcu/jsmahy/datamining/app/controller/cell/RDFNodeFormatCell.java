package cz.zcu.jsmahy.datamining.app.controller.cell;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class RDFNodeFormatCell extends ListCell<RDFNode> {
	private static final Logger LOGGER = LogManager.getLogger(RDFNodeFormatCell.class);

	private final ListView<RDFNode> rdfList;

	public RDFNodeFormatCell(final ListView<RDFNode> rdfList) {
		this.rdfList = rdfList;

		// TODO: context menu for "add/continue line"
		final ContextMenu contextMenu = new ContextMenu();
		final MenuItem editItem = new MenuItem();

		// use resource bundle
		editItem.textProperty()
		        .bind(Bindings.format("Add line \"%s\"", itemProperty()));
		editItem.setOnAction(event -> {
			final RDFNode item = getItem();
			// code to edit item...
		});

		final MenuItem deleteItem = new MenuItem();
		// use resource bundle
		deleteItem.textProperty()
		          .bind(Bindings.format("Delete \"%s\"", itemProperty()));
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

	public static String formatRDFNode(RDFNode node) {
		if (node == null) {
			return "null";
		}
		if (node.isLiteral()) {
			return node.asLiteral()
			           .toString();
		}
		if (node.isResource()) {
			return node.asResource()
			           .getLocalName();
		}
		LOGGER.debug("RDFNode \"{}\"was neither literal or resource, using default toString method.", node);
		return node.toString();
	}

	private static String prettyFormat(RDFNode node) {
		return formatRDFNode(node).replaceAll("_", " ");
	}

	@Override
	protected void updateItem(final RDFNode item, final boolean empty) {
		super.updateItem(item, empty);
		setGraphic(null);
		setText(null);
		if (item == null) {
			return;
		}

		setText(prettyFormat(item));
	}
}
