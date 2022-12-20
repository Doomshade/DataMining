package cz.zcu.jsmahy.datamining.app.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.app.controller.cell.RDFNodeCellFactory;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.SparqlRequest;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since
 */
public class MainController<T extends RDFNode> implements Initializable {
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
    private TreeView<T> ontologyTreeView;

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

        final MultipleSelectionModel<TreeItem<T>> selectionModel = ontologyTreeView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);

        // show a web view on selection
        // TODO: this will likely be a popup
        selectionModel.selectedItemProperty()
                      .addListener(this::onSelection);

        // use custom cell factory for display
        ontologyTreeView.setCellFactory(lv -> new RDFNodeCellFactory<>(lv, resources));
        ontologyTreeView.setEditable(false);
    }

    /**
     * Callback for {@link SelectionModel#selectedItemProperty()} in the ontology list view.
     *
     * @param observable the observable that was invalidated
     */
    private void onSelection(final Observable observable) {
        final TreeItem<T> selectedItem = ontologyTreeView.getSelectionModel()
                                                         .getSelectedItem();
        if (selectedItem == null) {
            LOGGER.info("Could not handle ontology click because selected item was null.");
            return;
        }

        if (selectedItem == ontologyTreeView.getRoot()) {
            return;
        }

        final String formattedItem = RDFNodeCellFactory.formatRDFNode(selectedItem.getValue());
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

        final Node node = NodeFactory.createLiteral("Dynastie hovad");
        final RDFNode literal = new LiteralImpl(node, null);
        final TreeItem<T> root = new TreeItem<>((T) literal);
        ontologyTreeView.setRoot(root);
        ontologyTreeView.setShowRoot(node.isLiteral() && !node.getLiteral()
                                                              .getLexicalForm()
                                                              .isBlank());
        root.setExpanded(true);
        final ObservableList<TreeItem<T>> children = root.getChildren();
        children.clear();
        children.addListener(new ListChangeListener<TreeItem<T>>() {
            @Override
            public void onChanged(final Change<? extends TreeItem<T>> c) {
                while (c.next()) {
                    if (!c.wasAdded()) {
                        return;
                    }
                    final ObservableList<? extends TreeItem<T>> list = c.getList();
                    final Set<RDFNode> set = new HashSet<>();
                    list.removeIf(child -> !set.add(child.getValue()));
                }
            }
        });

        final Injector injector = Guice.createInjector(new DBPediaModule());

        final RequestHandler<T, Void> dbPediaRequestHandler = injector.getInstance(RequestHandler.class);
        final DataNodeFactory<T> dataNodeFactory = injector.getInstance(DataNodeFactory.class);
        final SparqlRequest<T> request = new SparqlRequest<>(searchValue, "http://dbpedia.org/ontology/", "predecessor", ontologyTreeView.getRoot(), dataNodeFactory.newRoot(null));
        final Service<Void> query = dbPediaRequestHandler.query(request);
        query.setOnSucceeded(x -> {

            ontologyTreeView.getSelectionModel()
                            .selectFirst();

        });
        query.setOnFailed(x -> {
            query.getException()
                 .printStackTrace();
        });
        query.restart();

        this.rootPane.disableProperty()
                     .bind(query.runningProperty());
        this.progressIndicator.visibleProperty()
                              .bind(query.runningProperty());
        progressIndicator.progressProperty()
                         .bind(query.progressProperty());
    }

    private Alert buildExceptionAlert(final Throwable e) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        // use resource bundle
        alert.setTitle("Error");
        alert.setResizable(true);
        alert.contentTextProperty()
             .addListener(new ChangeListener<String>() {
                 @Override
                 public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
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
