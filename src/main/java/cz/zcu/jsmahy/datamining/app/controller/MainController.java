package cz.zcu.jsmahy.datamining.app.controller;

import com.jfoenix.controls.JFXListView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since
 */
public class MainController implements Initializable {

	@FXML
	private BorderPane rootPane;
	@FXML
	private JFXListView<String> ontologyListView;
	@FXML
	private WebView wikiPageWebView;

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {

	}

	public void handleOntologyClick(final MouseEvent mouseEvent) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setContentText("Not yet implemented");
		alert.show();
	}
}
