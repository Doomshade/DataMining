package cz.zcu.jsmahy.datamining.app.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.SparqlRequest;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class LinkController<T extends RDFNode> implements Initializable {
    private static final Logger LOGGER = LogManager.getLogger(LinkController.class);
    public JFXTextField searchField;
    public JFXButton testbtn;
    public JFXTextArea textArea;
    public JFXSpinner progress;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

    }

    @FXML
    public void search(final MouseEvent mouseEvent) {
        setDisabled(true);
        final Injector injector = Guice.createInjector(new DBPediaModule());
        final DataNodeFactory<T> nodeFactory = injector.getInstance(DataNodeFactory.class);

        String ontology = searchField.getText();
        final RequestHandler<T, Void> dbPediaRequestHandler = injector.getInstance(RequestHandler.class);
        SparqlRequest<T> request = new SparqlRequest<>(ontology, "http://dbpedia.org/ontology/", "successor", new TreeItem<>(), nodeFactory.newRoot());

        final Service<Void> query = dbPediaRequestHandler.query(request);
        query.setOnSucceeded(x -> {
            setDisabled(false);
            textArea.textProperty()
                    .set("");

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
