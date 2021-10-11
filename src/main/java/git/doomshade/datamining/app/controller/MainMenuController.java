package git.doomshade.datamining.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import git.doomshade.datamining.data.Ontology;
import git.doomshade.datamining.data.Request;
import git.doomshade.datamining.data.RequestHandlerFactory;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class MainMenuController implements Initializable {
    @FXML
    private JFXSpinner progress;
    @FXML
    private JFXTextArea textArea;

    @FXML
    private HBox hbox;
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private JFXButton testbtn;

    @FXML
    private JFXTextField searchField;

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
    }

    public void search(final MouseEvent mouseEvent) {
        setVisibilityAndOpacity(true, 0.2);


        String ontology = searchField.getText();
        Request request = new Request(
                ontology,
                "http://dbpedia.org/ontology/",
                "successor"
        );
        Service<Ontology> query = RequestHandlerFactory.getDBPediaRequestHandler().query(request);

        query.setOnSucceeded(x -> {
            setVisibilityAndOpacity(false, 1);
            final Ontology ont = (Ontology) x.getSource().getValue();
            //query.getException().printStackTrace();
            ont.printOntology(System.out);
            StringBuilder sb = new StringBuilder();
            ont.printOntology(sb);
            textArea.textProperty().set(sb.toString());

        });
        query.setOnFailed(x -> {
            setVisibilityAndOpacity(false, 1);
            query.getException().printStackTrace();
        });
        query.restart();
        progress.progressProperty().bind(query.progressProperty());
    }

    private void setVisibilityAndOpacity(boolean disabled, double opacity) {
        testbtn.setDisable(disabled);
        progress.setVisible(disabled);
        searchField.setDisable(disabled);
        textArea.setDisable(disabled);
    }
}
