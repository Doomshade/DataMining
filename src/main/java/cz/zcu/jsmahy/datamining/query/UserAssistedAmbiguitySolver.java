package cz.zcu.jsmahy.datamining.query;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.DataNodeReference;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaAmbiguitySolver;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeListCellFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Optional;

/**
 * A user assisted ambiguity solver. The solver prompts the user using a simple GUI to choose the right target node.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class UserAssistedAmbiguitySolver<T extends RDFNode> implements DBPediaAmbiguitySolver<T, Void> {

    private final DataNodeFactory<T> nodeFactory;

    public UserAssistedAmbiguitySolver() {
        Injector injector = Guice.createInjector(new DBPediaModule());
        this.nodeFactory = injector.getInstance(DataNodeFactory.class);
    }

    @Override
    public DataNodeReference<T> call(final ObservableList<DataNode<T>> list, final RequestHandler<T, Void> requestHandler) {
        DataNodeReference<T> ref = new DataNodeReference<>();
        Platform.runLater(() -> new DialogueHandler(list, ref, requestHandler).showDialogueAndWait());
        return ref;
    }

    private class DialogueHandler {
        private final Dialog<DataNode<T>> node = new Dialog<>();
        private final DialogPane dialogPane = node.getDialogPane();
        private final DataNodeReference<T> ref;
        private final ListView<DataNode<T>> content = new ListView<>();
        private final RequestHandler<T, Void> requestHandler;

        {
            dialogPane.getButtonTypes()
                      .addAll(ButtonType.OK, ButtonType.CANCEL);
            content.setCellFactory(x -> new RDFNodeListCellFactory<>());
            node.setResultConverter(buttonType -> content.getSelectionModel()
                                                         .getSelectedItem());
        }

        public DialogueHandler(final ObservableList<DataNode<T>> list, final DataNodeReference<T> ref, final RequestHandler<T, Void> requestHandler) {
            this.ref = ref;
            this.content.setItems(list);
            this.dialogPane.setContent(content);
            this.requestHandler = requestHandler;
        }

        public void showDialogueAndWait() {
            // show the dialogue and wait for response
            final Optional<DataNode<T>> result = node.showAndWait();
            ref.set(result.orElse(null));
            ref.setHasMultipleReferences(result.isEmpty());

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            ref.finish();
            requestHandler.unlockDialogPane();
        }
    }
}
