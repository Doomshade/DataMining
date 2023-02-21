package cz.zcu.jsmahy.datamining.resolvers;

import cz.zcu.jsmahy.datamining.api.DefaultResponseResolver;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTask;
import javafx.application.Platform;
import org.apache.jena.rdf.model.Statement;

import java.util.Collection;
import java.util.ResourceBundle;


/**
 * A user assisted ambiguity solver. The solver prompts the user using a simple GUI to choose the right target node.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class MultipleItemChoiceResolver extends DefaultResponseResolver<Collection<Statement>> {

    public static final String RESULT_KEY_CHOSEN_RDF_NODE = "chosenNextRDFNode";

    @Override
    protected void resolveInternal(final Collection<Statement> lineContinuationCandidates, final SparqlEndpointTask<?> requestHandler) {
        // first off we check if we have an ontology path set
        // if not, pop up a dialogue
        Platform.runLater(() -> {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
            final RDFNodeChooserDialog dialog = new RDFNodeChooserDialog(lineContinuationCandidates, x -> true, resourceBundle.getString("ambiguity-dialog-title"), "");
            dialog.showDialogueAndWait(statement -> result.addMetadata(RESULT_KEY_CHOSEN_RDF_NODE, statement.getObject()));

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            markResponseReady();
            requestHandler.unlockDialogPane();
        });
    }
}
