package cz.zcu.jsmahy.datamining.resolvers;

import cz.zcu.jsmahy.datamining.Main;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeListCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * A user assisted ambiguity solver. The solver prompts the user using a simple GUI to choose the right target node.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class MultipleItemChoiceResolver<R> implements BlockingResponseResolver<R, BlockingDataNodeReferenceHolder> {

    private static final Logger LOGGER = LogManager.getLogger(MultipleItemChoiceResolver.class);

    @Override
    public BlockingDataNodeReferenceHolder resolveRequest(final List<DataNode> ambiguousInput, final QueryData inputMetadata, final SparqlEndpointTask<R> requestHandler) {
        // first off we check if we have an ontology path set
        // if not, pop up a dialogue
        final BlockingDataNodeReferenceHolder ref = new BlockingDataNodeReferenceHolder();

        Platform.runLater(() -> {
            final MultipleItemChoiceDialog dialog = new MultipleItemChoiceDialog(ambiguousInput, ref, x -> new RDFNodeListCellFactory(), SelectionMode.SINGLE);
            dialog.showDialogueAndWait();

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            ref.finish();
            requestHandler.unlockDialogPane();
        });
        return ref;
    }

    private class MultipleItemChoiceDialog {
        private final Dialog<List<DataNode>> dialog = new Dialog<>();
        private final DialogPane dialogPane = dialog.getDialogPane();
        private final ListView<DataNode> content = new ListView<>();
        private final DataNodeReferenceHolder ref;

        public MultipleItemChoiceDialog(final List<DataNode> list,
                                        final DataNodeReferenceHolder ref,
                                        final Callback<ListView<DataNode>, ListCell<DataNode>> cellFactory,
                                        final SelectionMode selectionMode) {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
            this.ref = ref;

            // setup dialog, such as buttons, title etc
            this.dialogPane.getButtonTypes()
                           .addAll(ButtonType.OK, ButtonType.CANCEL);
            this.dialog.initOwner(Main.getPrimaryStage());
            this.dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.CANCEL) {
                    return null;
                }
                if (buttonType == ButtonType.OK) {
                    return content.getSelectionModel()
                                  .getSelectedItems();
                }
                LOGGER.error("Unrecognized button type: {}", buttonType);
                return null;
            });
            this.dialog.setTitle(resourceBundle.getString("ambiguity-dialog-title"));
            this.dialog.setOnShown(event -> Platform.runLater(() -> {
                this.content.requestFocus();
                this.content.getSelectionModel()
                            .selectFirst();
            }));

            // dialog content
            this.content.setCellFactory(cellFactory);
            this.content.setItems(FXCollections.observableArrayList(list));
            final MultipleSelectionModel<DataNode> selectionModel = this.content.getSelectionModel();
            selectionModel.setSelectionMode(selectionMode);
            this.content.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    final Button button = (Button) dialogPane.lookupButton(ButtonType.OK);
                    button.fire();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    final Button button = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
                    button.fire();
                }
            });
            this.dialogPane.setContent(content);

        }

        public void showDialogueAndWait() {
            // show the dialogue and wait for response
            final Optional<List<DataNode>> result = dialog.showAndWait();
            result.ifPresent(ref::set);
        }
    }
}
