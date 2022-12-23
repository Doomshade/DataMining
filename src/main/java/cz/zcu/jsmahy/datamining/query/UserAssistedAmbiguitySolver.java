package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaAmbiguitySolver;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeListCellFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import org.apache.jena.rdf.model.RDFNode;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class UserAssistedAmbiguitySolver<T extends RDFNode> implements DBPediaAmbiguitySolver<T, Void> {

    @Override
    public AtomicReference<DataNode<T>> call(final ObservableList<DataNode<T>> list, final RequestHandler<T, Void> requestHandler) {
        AtomicReference<DataNode<T>> ref = new AtomicReference<>();
        Platform.runLater(() -> new DialogueHandler(list, ref, requestHandler).showDialogueAndWait());
        return ref;
    }

    private class DialogueHandler {
        private final Dialog<DataNode<T>> node = new Dialog<>();
        private final DialogPane dialogPane = node.getDialogPane();
        private final AtomicReference<DataNode<T>> ref;
        private final ListView<DataNode<T>> content = new ListView<>();
        private final RequestHandler<T, Void> requestHandler;

        {
            dialogPane.getButtonTypes()
                      .addAll(ButtonType.OK, ButtonType.CANCEL);
            content.setCellFactory(x -> new RDFNodeListCellFactory<>());
            node.setResultConverter(buttonType -> content.getSelectionModel()
                                                         .getSelectedItem());
        }

        public DialogueHandler(final ObservableList<DataNode<T>> list, final AtomicReference<DataNode<T>> ref, final RequestHandler<T, Void> requestHandler) {
            this.ref = ref;
            this.content.setItems(list);
            this.dialogPane.setContent(content);
            this.requestHandler = requestHandler;
        }

        public void showDialogueAndWait() {
            // show the dialogue and wait for response
            ref.set(node.showAndWait()
                        .orElse(null));

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            requestHandler.unlockDialogPane();
        }
    }
}
