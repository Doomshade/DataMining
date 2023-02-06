package cz.zcu.jsmahy.datamining.resolvers;

import cz.zcu.jsmahy.datamining.Main;
import cz.zcu.jsmahy.datamining.api.BlockingDataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.api.DataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.api.DefaultResponseResolver;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTask;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeListCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * A user assisted ambiguity solver. The solver prompts the user using a simple GUI to choose the right target node.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class MultipleItemChoiceResolver extends DefaultResponseResolver<Collection<RDFNode>> {

    public static final String RESULT_KEY_CHOSEN_RDF_NODE = "chosenNextRDFNode";
    private static final Logger LOGGER = LogManager.getLogger(MultipleItemChoiceResolver.class);

    @Override
    public void resolve(final Collection<RDFNode> lineContinuationCandidates, final SparqlEndpointTask<?> requestHandler) {
        // first off we check if we have an ontology path set
        // if not, pop up a dialogue
        final BlockingDataNodeReferenceHolder ref = new BlockingDataNodeReferenceHolder();

        Platform.runLater(() -> {
            final MultipleItemChoiceDialog dialog = new MultipleItemChoiceDialog(lineContinuationCandidates, ref, x -> new RDFNodeListCellFactory(), SelectionMode.SINGLE);
            dialog.showDialogueAndWait()
                  .ifPresent(res -> {
                      result.addMetadata(RESULT_KEY_CHOSEN_RDF_NODE, res.get(0));
                  });

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            markResponseReady();
            requestHandler.unlockDialogPane();
        });
    }

    private class MultipleItemChoiceDialog {
        private final Dialog<List<RDFNode>> dialog = new Dialog<>();
        private final DialogPane dialogPane = dialog.getDialogPane();
        private final ListView<RDFNode> content = new ListView<>();
        private final DataNodeReferenceHolder ref;

        public MultipleItemChoiceDialog(final Collection<RDFNode> lineContinuationCandidates,
                                        final DataNodeReferenceHolder ref,
                                        final Callback<ListView<RDFNode>, ListCell<RDFNode>> cellFactory,
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
            this.content.setItems(FXCollections.observableArrayList(lineContinuationCandidates));
            final MultipleSelectionModel<RDFNode> selectionModel = this.content.getSelectionModel();
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

        public Optional<List<RDFNode>> showDialogueAndWait() {
            // show the dialogue and wait for response
            final Optional<List<RDFNode>> result = dialog.showAndWait();
            result.ifPresent(ref::set);
            return result;
        }
    }
}
