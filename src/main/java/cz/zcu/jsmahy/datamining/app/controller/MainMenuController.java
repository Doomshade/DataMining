package cz.zcu.jsmahy.datamining.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class MainMenuController implements Initializable {
    @FXML
    public VBox rootPane;
    @FXML
    public JFXButton createOntologyBtn;
    @FXML
    public JFXButton loadOntologyBtn;
    public JFXComboBox<String> sourceChooseComboBox;

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        //createOntologyBtn.getStylesheets().add("/css/main.css");
        sourceChooseComboBox
                .getItems()
                .setAll(
                        resourceBundle.getString("online"),
                        resourceBundle.getString("offline"));
        sourceChooseComboBox
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    System.out.println("Old val: " + oldValue);
                    System.out.println("New val: " + newValue);
                });
    }

    public void handleCreateOntologyBtn(final MouseEvent mouseEvent) {
        System.out.println(mouseEvent.getX());
        System.out.println(mouseEvent.getY());
    }

    public void handleLoadOnotologyBtn(final MouseEvent mouseEvent) {

    }
}
