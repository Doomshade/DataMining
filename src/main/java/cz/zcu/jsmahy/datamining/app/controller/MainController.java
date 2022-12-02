package cz.zcu.jsmahy.datamining.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import cz.zcu.jsmahy.datamining.QueryService;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since
 */
public class MainController implements Initializable {
    /*
PREFIX rdf: <https://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX r: <http://dbpedia.org/resource/>
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
select distinct ?name
{
?pred dbp:predecessor <http://dbpedia.org/resource/Charles_IV,_Holy_Roman_Emperor> .
?pred dbp:predecessor+ ?name
}
order by ?pred
     */
    private static final String WIKI_URL = "https://wikipedia.org/wiki/%s";
    private static final Logger LOGGER = LogManager.getLogger(MainController.class);
    private static final String DBPEDIA_SERVICE = "http://dbpedia.org/sparql/";
    private static final int LINE_BREAK_LIMIT = 20;
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
        ontologyListView.setCellFactory(lv -> new RDFNodeCellFactory(lv, resources));
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
        final String searchValue = searchField.getText();
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
                                                           .append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n")
                                                           .append("PREFIX r: <http://dbpedia.org/resource/>\n")
                                                           .append("PREFIX dbo: <http://dbpedia.org/ontology/>\n")
                                                           .append("PREFIX dbp: <http://dbpedia.org/property/>\n")
                                                           .append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n")
                                                           .append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n")
                                                           .append("PREFIX bif: <http://www.openlinksw.com/schemas/bif#>")
                                                           .append("select distinct ?first ?next\n")
                                                           .append("{\n")
                                                           .append("?first rdfs:label ?name .")
                                                           .append("FILTER (?name = \"")
                                                           .append(searchValue)
                                                           .append("\"@cs) .\n")
                                                           .append("?first dbp:precededBy+ ?next .")
                                                           .append("}\n")
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
                    final Marker marker = MarkerManager.getMarker("query");
                    boolean first = true;
                    while (results.hasNext()) {
                        final QuerySolution solution = results.next();
                        final Iterator<String> it = solution.varNames();
                        LOGGER.debug("Variables:");
                        while (it.hasNext()) {
                            LOGGER.debug(it.next());
                        }
                        LOGGER.debug("---------");
                        if (first) {
                            final RDFNode firstNode = solution.get("first");
                            LOGGER.debug(marker, "First: {}", firstNode);
                            items.add(firstNode);
                            first = false;
                        }
                        final RDFNode resource = solution.get("next");
                        LOGGER.debug(marker, "Next: {}", resource);
                        items.add(resource);
                    }
                    ontologyListView.getSelectionModel()
                                    .selectFirst();
                    hideIndicator(true);
                });

                queryService.setOnFailed(e -> {
                    hideIndicator(true);
                    final Throwable ex = queryService.getException();
                    LOGGER.error(ex);
                    final Alert alert = buildExceptionAlert(ex);
                    alert.show();
                });
                queryService.setOnCancelled(e -> {
                    hideIndicator(true);
                });
                queryService.start();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    final Alert alert = buildExceptionAlert(e);
                    alert.show();
                });
                LOGGER.error(e);
                hideIndicator(true);
            }

        });
        t.start();
    }

    private Alert buildExceptionAlert(final Throwable e) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        // use resource bundle
        alert.setTitle("Error");
        alert.setResizable(true);
        alert.contentTextProperty()
             .addListener(new ChangeListener<String>() {
                 @Override
                 public void changed(final ObservableValue<? extends String> observable, final String oldValue,
                                     final String newValue) {
                     int lineBreaks = 0;
                     final char[] chars = newValue.toCharArray();
                     for (int i = 0; i < chars.length; i++) {
                         final char c = chars[i];
                         if (c == '\n') {
                             lineBreaks++;
                             if (lineBreaks >= LINE_BREAK_LIMIT) {
                                 alert.setContentText(newValue.substring(0, i)
                                                              .concat(" (...)"));
                                 return;
                             }
                         }
                     }
                 }
             });
        alert.setResult(ButtonType.OK);
        // use resource bundle
        alert.setHeaderText("An error occurred when searching for " + searchField.getText());
        alert.setContentText(e.getMessage());
        return alert;
    }

    private void hideIndicator(final boolean hide) {
        progressIndicator.setVisible(!hide);
//		progressIndicator.setProgress(-1);
//		progressIndicator.setDisable(hide);
        LOGGER.debug("{} indicator.", hide ? "Hiding" : "Showing");
    }
}
