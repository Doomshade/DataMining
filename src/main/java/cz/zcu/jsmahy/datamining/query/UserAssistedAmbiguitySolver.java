package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.Main;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReference;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaAmbiguitySolver;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeListCellFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A user assisted ambiguity solver. The solver prompts the user using a simple GUI to choose the right target node.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class UserAssistedAmbiguitySolver<T extends RDFNode> implements DBPediaAmbiguitySolver<T, Void> {

    private static final Logger LOGGER = LogManager.getLogger(UserAssistedAmbiguitySolver.class);

    @Override
    public DataNodeReference<T> call(final ObservableList<DataNode<T>> list, final RequestHandler<T, Void> requestHandler) {
        DataNodeReference<T> ref = new DataNodeReference<>();
        Platform.runLater(() -> new DialogueHandler(list, ref, requestHandler).showDialogueAndWait());
        return ref;
    }

    private class DialogueHandler {
        private final Dialog<DataNode<T>> dialog = new Dialog<>();
        private final DialogPane dialogPane = dialog.getDialogPane();
        private final DataNodeReference<T> ref;
        private final ListView<DataNode<T>> content = new ListView<>();
        private final RequestHandler<T, Void> requestHandler;

        {
            dialogPane.getButtonTypes()
                      .addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.initOwner(Main.getPrimaryStage());
            content.setCellFactory(x -> new RDFNodeListCellFactory<>());
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.CANCEL) {
                    return null;
                }
                if (buttonType == ButtonType.OK) {
                    return content.getSelectionModel()
                                  .getSelectedItem();
                }
                LOGGER.error("Unrecognized button type: {}", buttonType);
                return null;
            });
        }

        public DialogueHandler(final ObservableList<DataNode<T>> list, final DataNodeReference<T> ref, final RequestHandler<T, Void> requestHandler) {
            this.ref = ref;
            this.content.setItems(list);
            this.content.getSelectionModel()
                        .setSelectionMode(SelectionMode.MULTIPLE);
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

            this.dialog.setTitle("Vyberte prosím, po kom dál jít. Díky!");
            this.dialog.setOnShown(event -> {
                Platform.runLater(() -> {
                    this.content.requestFocus();
                    this.content.getSelectionModel()
                                .selectFirst();
                    LOGGER.info("FOCUSING BRO");
                });
            });
            this.requestHandler = requestHandler;
        }

        public void showDialogueAndWait() {
            // show the dialogue and wait for response
            final DataNode<T> result = dialog.showAndWait()
                                             .orElse(null);
            ref.set(result);
            ref.setHasMultipleReferences(result == null);

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            ref.finish();
            requestHandler.unlockDialogPane();
        }
    }
}
