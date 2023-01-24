package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.Main;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeListCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
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
public class UserAssistedAmbiguousInputResolver<T extends RDFNode> implements BlockingAmbiguousInputResolver<T, Void> {

    private static final Logger LOGGER = LogManager.getLogger(UserAssistedAmbiguousInputResolver.class);

    @Override
    public BlockingDataNodeReferenceHolder<T> resolveRequest(final List<DataNode<T>> ambiguousInput, final QueryData inputMetadata, final RequestHandler<T, Void> requestHandler) {
        // first off we check if we have an ontology path set
        // if not, pop up a dialogue
        final BlockingDataNodeReferenceHolder<T> ref = new BlockingDataNodeReferenceHolder<>();
        final Property ontologyPathPredicate = inputMetadata.getOntologyPathPredicate();

        if (ontologyPathPredicate == null) {
            Platform.runLater(() -> {
                final OntologyPathPredicateChoiceDialog dialog = new OntologyPathPredicateChoiceDialog(ref);
                dialog.showDialogueAndWait();

                ref.finish();
                requestHandler.unlockDialogPane();
            });
            return ref;
        }

        Platform.runLater(() -> {
            final MultipleItemChoiceDialog dialog = new MultipleItemChoiceDialog(ambiguousInput, ref, x -> new RDFNodeListCellFactory<>(), SelectionMode.SINGLE);
            dialog.showDialogueAndWait();

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            ref.finish();
            requestHandler.unlockDialogPane();
        });
        return ref;
    }

    private class OntologyPathPredicateChoiceDialog {
        private final Dialog<List<DataNode<T>>> dialog = new Dialog<>();
        private final TableView<RDFNodeModel> content = new TableView<>();
        private final DataNodeReferenceHolder<T> ref;

        private OntologyPathPredicateChoiceDialog(final DataNodeReferenceHolder<T> ref) {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
            this.ref = ref;

            // setup dialog, such as buttons, title etc
            final DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getButtonTypes()
                      .addAll(ButtonType.OK, ButtonType.CANCEL);
            this.dialog.initOwner(Main.getPrimaryStage());
            this.dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.CANCEL) {
                    return null;
                }
                if (buttonType == ButtonType.OK) {
                    return content.getSelectionModel()
                                  .getSelectedItems()
                                  .stream()
                                  .map(x -> x.node)
                                  .toList();
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
        }

        public void showDialogueAndWait() {
            // show the dialogue and wait for response
            final List<DataNode<T>> result = dialog.showAndWait()
                                                   .orElse(null);
            ref.set(result);
        }

        private class RDFNodeModel {
            private final DataNode<T> node;

            private RDFNodeModel(final DataNode<T> node) {
                this.node = node;
            }
        }
    }

    private class MultipleItemChoiceDialog {
        private final Dialog<List<DataNode<T>>> dialog = new Dialog<>();
        private final DialogPane dialogPane = dialog.getDialogPane();
        private final ListView<DataNode<T>> content = new ListView<>();
        private final DataNodeReferenceHolder<T> ref;

        public MultipleItemChoiceDialog(final List<DataNode<T>> list, final DataNodeReferenceHolder<T> ref, final Callback<ListView<DataNode<T>>, ListCell<DataNode<T>>> cellFactory,
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
            final MultipleSelectionModel<DataNode<T>> selectionModel = this.content.getSelectionModel();
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
            final Optional<List<DataNode<T>>> result = dialog.showAndWait();
            result.ifPresent(ref::set);
        }
    }
}
