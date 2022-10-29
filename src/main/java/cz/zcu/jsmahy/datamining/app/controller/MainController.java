package cz.zcu.jsmahy.datamining.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import cz.zcu.jsmahy.datamining.QueryService;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeFormatCell;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since
 */
public class MainController implements Initializable {
	public static final String WIKI_URL = "https://wikipedia.org/wiki/%s";
	private static final Logger LOGGER = LogManager.getLogger(MainController.class);
	private static final String DBPEDIA_SERVICE = "http://dbpedia.org/sparql/";
	@FXML
	private JFXTextField searchField;
	@FXML
	private JFXButton searchButton;
	@FXML
	private BorderPane rootPane;
	@FXML
	private ListView<RDFNode> ontologyListView;
	@FXML
	private WebView wikiPageWebView;
	@FXML
	private JFXSpinner progressIndicator;

	private static synchronized void exit() {
		LOGGER.info("Exiting application...");
		Platform.exit();
		System.exit(0);
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
//		rootPane.setRight(null);
//		rootPane.setBottom(null);
		searchField.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				search(null);
			}
		});
		searchField.requestFocus();
		rootPane.setPadding(new Insets(10));
		ontologyListView.setEditable(false);
		ontologyListView.getSelectionModel()
		                .setSelectionMode(SelectionMode.SINGLE);
		ontologyListView.setCellFactory(RDFNodeFormatCell::new);
		rootPane.disableProperty()
		        .bind(progressIndicator.visibleProperty());
		ontologyListView.getSelectionModel()
		                .selectedItemProperty()
		                .addListener(this::onSelection);
//		wikiPageWebView.setMinHeight(960);
//		wikiPageWebView.setMinWidth(540);
	}

	private void onSelection(final Observable observable) {
		final RDFNode selectedItem = ontologyListView.getSelectionModel()
		                                             .getSelectedItem();
		if (selectedItem == null) {
			LOGGER.info("Could not handle ontology click because selected item was null.");
			return;
		}
		final String formattedItem = RDFNodeFormatCell.formatRDFNode(selectedItem);

		wikiPageWebView.getEngine()
		               .load(String.format(WIKI_URL, formattedItem));
	}

	public void handleOntologyClick(final MouseEvent mouseEvent) {

	}


	public void search(final MouseEvent mouseEvent) {
		final String searchValue = "r:" + searchField.getText()
		                                             .replaceAll(" ", "_");
		if (searchValue.isBlank()) {
			LOGGER.info("Search field is blank, not searching for anything.");
			final Alert alert = new Alert(Alert.AlertType.INFORMATION);
			// use resource bundle
			alert.setTitle("Invalid search");
			alert.setContentText("Please enter some text to search.");
			return;
		}

		final Thread t = new Thread(() -> {
			hideIndicator(false);
			try {
				LOGGER.debug("Building query");
				final String rawQuery = new StringBuilder().append("PREFIX rdf: <https://www.w3.org/1999/02/22-rdf-syntax-ns#>\n")
				                                           .append("PREFIX r: <http://dbpedia.org/resource/>\n")
				                                           .append("PREFIX dbo: <http://dbpedia.org/ontology/>\n")
				                                           .append("PREFIX dbp: <http://dbpedia.org/property/>\n")
				                                           .append("select distinct ?name\n")
				                                           .append("{\n")
				                                           .append("?pred dbp:predecessor ")
				                                           .append(searchValue)
				                                           .append(" .\n")
				                                           .append("?pred dbp:predecessor+ ?name\n")
				                                           .append("}\n")
				                                           .append("order by ?pred")
				                                           .toString();
				LOGGER.debug("Raw query:\n{}", rawQuery);

				// build the query via Jena
				final Query query = QueryFactory.create(rawQuery);
				final QueryExecution qe = QueryExecution.service(DBPEDIA_SERVICE)
				                                        .query(query)
				                                        .build();
				LOGGER.info("SPARQL endpoint: {}", DBPEDIA_SERVICE);

				// execute the query on a separate thread via Service
				final Service<ResultSet> queryService = new QueryService(qe);
				this.progressIndicator.progressProperty()
				                      .bind(queryService.progressProperty());
				queryService.setOnSucceeded(e -> {
					final ResultSet results = (ResultSet) e.getSource()
					                                       .getValue();
					LOGGER.info("Query successfully executed");
					LOGGER.info("Query returned {} results", results.hasNext() ? "some" : "no");

					final ObservableList<RDFNode> items = ontologyListView.getItems();
					items.clear();
					// print the results
					while (results.hasNext()) {
						final QuerySolution soln = results.next();
						final RDFNode resource = soln.get("name");
						items.add(resource);
					}
					ontologyListView.getSelectionModel()
					                .selectFirst();
					hideIndicator(true);
				});

				queryService.setOnFailed(e -> {
					hideIndicator(true);
					LOGGER.error(queryService.getException());
					exit();
				});
				queryService.setOnCancelled(e -> {
					hideIndicator(true);
					exit();
				});

				queryService.start();
			} catch (Exception e) {
				Platform.runLater(() -> {
					final Alert alert = new Alert(Alert.AlertType.ERROR);
					// use resource bundle
					alert.setTitle("Error");
					alert.setResizable(true);
					alert.setResult(ButtonType.OK);
					// use resource bundle
					alert.setHeaderText("An error occurred when searching for " + searchField.getText());
					alert.setContentText(e.getMessage());
					alert.show();
				});
				LOGGER.error(e);
				hideIndicator(true);
			}

		});
		t.start();
	}

	private void hideIndicator(final boolean hide) {
		progressIndicator.setVisible(!hide);
//		progressIndicator.setProgress(-1);
//		progressIndicator.setDisable(hide);
		LOGGER.debug("{} indicator.", hide ? "Hiding" : "Showing");
	}
}
