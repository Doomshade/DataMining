package cz.zcu.jsmahy.datamining.app.controller.cell;

import cz.zcu.jsmahy.datamining.api.DataNode;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.apache.jena.rdf.model.RDFNode;

import static cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory.formatRDFNode;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class RDFNodeListCellFactory<T extends RDFNode> extends ListCell<DataNode<T>> {
    public static final String SPECIAL_CHARACTERS = "_";

    /**
     * <p>Formats the {@link RDFNode} for pretty output in the {@link ListView}.</p>
     * <p>{@link RDFNodeCellFactory#formatRDFNode(RDFNode)} preserves special characters such as "_" - this method gets rids of those</p>
     *
     * @param node the node to format. if null, {@code "null"} is returned.
     *
     * @see RDFNodeCellFactory#formatRDFNode(RDFNode)
     */
    private String prettyFormat(DataNode<T> node) {
        return formatRDFNode(node.getData()).replaceAll(SPECIAL_CHARACTERS, " ");
    }

    @Override
    protected void updateItem(final DataNode<T> item, final boolean empty) {
        super.updateItem(item, empty);
        setText(item == null ? null : prettyFormat(item));
    }
}
