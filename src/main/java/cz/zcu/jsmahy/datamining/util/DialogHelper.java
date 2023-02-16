package cz.zcu.jsmahy.datamining.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.util.function.Consumer;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DialogHelper {
    public DialogHelper() {
    }

    public void textInputDialog(final String labelText, final String title, final Consumer<String> dialogFinishHandler) {
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
        dialog.setOnShown(shownEvent -> Platform.runLater(() -> {
            textField.requestFocus();
            shownEvent.consume();
        }));
        dialog.showAndWait()
              .ifPresent(dialogFinishHandler);
    }

}
