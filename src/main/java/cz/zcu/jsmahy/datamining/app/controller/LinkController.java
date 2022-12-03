package cz.zcu.jsmahy.datamining.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import cz.zcu.jsmahy.datamining.query.Ontology;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.RequestHandlerFactory;
import cz.zcu.jsmahy.datamining.query.SparqlRequest;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	private static final Logger LOGGER = LogManager.getLogger(LinkController.class);

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {

	}

	@FXML
	public void search(final MouseEvent mouseEvent) {
		setDisabled(true);

		String ontology = searchField.getText();
		SparqlRequest request = new SparqlRequest(ontology, "http://dbpedia.org/ontology/", "successor");

		Service<Ontology> query = RequestHandlerFactory.getDBPediaRequestHandler()
		                                               .query(request);
		query.setOnSucceeded(x -> {
			final Ontology ont = (Ontology) x.getSource()
			                                 .getValue();
			LOGGER.info("Printing ontology: {}", ont.toString());
			setDisabled(false);
			System.out.println(ont);
			final StringBuilder sb = new StringBuilder();
			ont.printOntology(sb);
			textArea.textProperty()
			        .set(sb.toString());

		});

		query.setOnFailed(x -> {
			query.getException()
			     .printStackTrace();
			setDisabled(false);
		});
		query.restart();
		progress.progressProperty()
		        .bind(query.progressProperty());
	}


	private void setDisabled(boolean disabled) {
		testbtn.setDisable(disabled);
		progress.setVisible(disabled);
		searchField.setDisable(disabled);
		textArea.setDisable(disabled);
	}
}
