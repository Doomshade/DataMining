package cz.zcu.jsmahy.datamining.util;

import com.jfoenix.controls.JFXListView;
import cz.zcu.jsmahy.datamining.Main;
import cz.zcu.jsmahy.datamining.api.BlockingDataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.query.BlockingRequestHandler;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DialogHelper {
    public DialogHelper() {
    }

    public class ItemChooseDialog<T, R> {
        private final Dialog<List<DataNode<T>>> dialog = new Dialog<>();
        private final DialogPane dialogPane = dialog.getDialogPane();
        private final ListView<DataNode<T>> content = new JFXListView<>();
        private final DataNodeReferenceHolder<T> ref;
        private final RequestHandler<T, R> requestHandler;

        private ItemChooseDialog(final DataNodeReferenceHolder<T> ref, final RequestHandler<T, R> requestHandler, final Callback<ListView<DataNode<T>>, ListCell<DataNode<T>>> cellFactory,
                                 final ObservableList<DataNode<T>> list, final SelectionMode selectionMode) {
            this.ref = ref;
            this.requestHandler = requestHandler;

            // setup dialog, such as buttons, title etc
            dialogPane.getButtonTypes()
                      .addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.initOwner(Main.getPrimaryStage());
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.CANCEL) {
                    return null;
                }
                if (buttonType == ButtonType.OK) {
                    return content.getSelectionModel()
                                  .getSelectedItems();
                }
                return null;
            });
            dialog.setTitle("Title");
            dialog.setOnShown(event -> Platform.runLater(() -> {
                content.requestFocus();
                content.getSelectionModel()
                       .selectFirst();
            }));

            // dialog content
            content.setCellFactory(cellFactory);
            content.setItems(list);
            content.getSelectionModel()
                   .setSelectionMode(selectionMode);
            content.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    final Button button = (Button) dialogPane.lookupButton(ButtonType.OK);
                    button.fire();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    final Button button = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
                    button.fire();
                }
            });
            dialogPane.setContent(content);
        }

        public void showDialogueAndWait() {
            // show the dialogue and wait for response
            final List<DataNode<T>> result = dialog.showAndWait()
                                                   .orElse(null);
            ref.set(result);

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            if (ref instanceof BlockingDataNodeReferenceHolder<T> blockingRef) {
                blockingRef.finish();
            }
            if (requestHandler instanceof BlockingRequestHandler<T, R> blockingRequestHandler) {
                blockingRequestHandler.unlockDialogPane();
            }
        }
    }

    public <T, R> ItemChooseDialog<T, R> itemChooseDialog(final DataNodeReferenceHolder<T> ref, final RequestHandler<T, R> requestHandler,
                                                          final Callback<ListView<DataNode<T>>, ListCell<DataNode<T>>> cellFactory,
                                                          final ObservableList<DataNode<T>> list, final SelectionMode selectionMode) {
        return new ItemChooseDialog<>(ref, requestHandler, cellFactory, list, selectionMode);
    }

    public void textInputDialog(final String labelText, final Consumer<String> dialogFinishHandler, final String title) {
        final TextField textField = new TextField();
        final Label label = new Label(labelText);
        final HBox hbox = new HBox(label, textField);
        hbox.setAlignment(Pos.CENTER_LEFT);
        label.setTextAlignment(TextAlignment.CENTER);
        hbox.setSpacing(5d);
        label.setLabelFor(textField);
        final Dialog<String> dialog = new Dialog<>();
        final DialogPane dialogPane = dialog.getDialogPane();
        dialog.setTitle(title);
        dialogPane.setContent(hbox);
        dialogPane.getButtonTypes()
                  .add(ButtonType.OK);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return textField.getText();
            }
            return null;
        });
        dialog.setOnShown(shownEvent -> {
            Platform.runLater(() -> {
                textField.requestFocus();
                shownEvent.consume();
            });
        });
        dialog.showAndWait()
              .ifPresent(dialogFinishHandler);
    }
}
