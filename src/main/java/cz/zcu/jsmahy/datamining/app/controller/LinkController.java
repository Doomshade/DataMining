package cz.zcu.jsmahy.datamining.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import cz.zcu.jsmahy.datamining.data.Ontology;
import cz.zcu.jsmahy.datamining.data.RequestHandlerFactory;
import cz.zcu.jsmahy.datamining.data.SparqlRequest;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class LinkController implements Initializable {
	public JFXTextField searchField;
	public JFXButton testbtn;
	public JFXTextArea textArea;
	public JFXSpinner progress;

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {

	}

	@FXML
	public void search(final MouseEvent mouseEvent) {
		setVisibilityAndOpacity(true, 0.2);

		String ontology = searchField.getText();
		SparqlRequest request = new SparqlRequest(ontology, "http://dbpedia.org/ontology/", "successor");
		Service<Ontology> query = RequestHandlerFactory.getDBPediaRequestHandler()
		                                               .query(request);
		RequestHandlerFactory.setupDefaultServiceHandlers(query);
		query.setOnSucceeded(x -> {
			query.getOnSucceeded()
			     .handle(x);
			setVisibilityAndOpacity(false, 1);
			StringBuilder sb = new StringBuilder();
			((Ontology) x.getSource()
			             .getValue()).printOntology(sb);
			textArea.textProperty()
			        .set(sb.toString());

		});

		query.setOnFailed(x -> {
			query.getOnFailed()
			     .handle(x);
			setVisibilityAndOpacity(false, 1);
		});
		query.restart();
		progress.progressProperty()
		        .bind(query.progressProperty());
	}

	private void setVisibilityAndOpacity(boolean disabled, double opacity) {
		testbtn.setDisable(disabled);
		progress.setVisible(disabled);
		searchField.setDisable(disabled);
		textArea.setDisable(disabled);
	}
}
