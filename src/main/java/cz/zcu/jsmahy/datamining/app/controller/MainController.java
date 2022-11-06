package cz.zcu.jsmahy.datamining.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import cz.zcu.jsmahy.datamining.QueryService;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
	private static final String WIKI_URL = "https://wikipedia.org/wiki/%s";
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
		// when user has focus on search field and presses enter -> search
		searchField.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				search();
			}
		});
		searchField.requestFocus();

		rootPane.setPadding(new Insets(10));
		// this will ensure the pane is disabled when progress indicator is visible
		rootPane.disableProperty()
		        .bind(progressIndicator.visibleProperty());

		final MultipleSelectionModel<RDFNode> selectionModel = ontologyListView.getSelectionModel();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.selectedItemProperty()
		              .addListener(this::onSelection);
		ontologyListView.setCellFactory(RDFNodeCellFactory::new);
		ontologyListView.setEditable(false);
	}

	/**
	 * Callback for {@link SelectionModel#selectedItemProperty()} in the ontology list view.
	 *
	 * @param observable the observable that was invalidated
	 */
	private void onSelection(final Observable observable) {
		final RDFNode selectedItem = ontologyListView.getSelectionModel()
		                                             .getSelectedItem();
		if (selectedItem == null) {
			LOGGER.info("Could not handle ontology click because selected item was null.");
			return;
		}

		final String formattedItem = RDFNodeCellFactory.formatRDFNode(selectedItem);
		wikiPageWebView.getEngine()
		               .load(String.format(WIKI_URL, formattedItem));
	}


	/**
	 * Handler for mouse press on the search button.
	 */
	public void search() {
		final String searchValue = searchField.getText()
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
				                                           .append("?pred dbp:predecessor <http://dbpedia.org/resource/")
				                                           .append(searchValue)
				                                           .append(">")
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
						LOGGER.debug(resource);
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
