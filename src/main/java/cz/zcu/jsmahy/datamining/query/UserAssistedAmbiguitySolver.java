package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.Main;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaAmbiguitySolver;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeListCellFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.ResourceBundle;

/**
 * A user assisted ambiguity solver. The solver prompts the user using a simple GUI to choose the right target node.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class UserAssistedAmbiguitySolver<T extends RDFNode> implements DBPediaAmbiguitySolver<T, Void> {

    private static final Logger LOGGER = LogManager.getLogger(UserAssistedAmbiguitySolver.class);

    @Override
    public DataNodeReferenceHolder<T> call(final ObservableList<DataNode<T>> list, final RequestHandler<T, Void> requestHandler) {
        DataNodeReferenceHolder<T> ref = new DataNodeReferenceHolder<>();
        Platform.runLater(() -> new DialogueHandler(list, ref, requestHandler, x -> new RDFNodeListCellFactory<>(), SelectionMode.SINGLE).showDialogueAndWait());
        return ref;
    }

    private class DialogueHandler {
        private final Dialog<List<DataNode<T>>> dialog = new Dialog<>();
        private final DialogPane dialogPane = dialog.getDialogPane();
        private final ListView<DataNode<T>> content = new ListView<>();
        private final DataNodeReferenceHolder<T> ref;
        private final RequestHandler<T, Void> requestHandler;

        public DialogueHandler(final ObservableList<DataNode<T>> list, final DataNodeReferenceHolder<T> ref, final RequestHandler<T, Void> requestHandler,
                               final Callback<ListView<DataNode<T>>, ListCell<DataNode<T>>> cellFactory, final SelectionMode selectionMode) {
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
            this.dialog.setOnShown(event -> {
                Platform.runLater(() -> {
                    this.content.requestFocus();
                    this.content.getSelectionModel()
                                .selectFirst();
                });
            });

            // dialog content
            this.content.setCellFactory(cellFactory);
            this.content.setItems(list);
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

            this.requestHandler = requestHandler;
        }

        public void showDialogueAndWait() {
            // show the dialogue and wait for response
            final List<DataNode<T>> result = dialog.showAndWait()
                                                   .orElse(null);
            ref.set(result);

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            ref.finish();
            requestHandler.unlockDialogPane();
        }
    }
}
