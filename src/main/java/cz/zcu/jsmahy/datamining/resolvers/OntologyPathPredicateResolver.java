package cz.zcu.jsmahy.datamining.resolvers;

import cz.zcu.jsmahy.datamining.api.DefaultResponseResolver;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTask;
import javafx.application.Platform;
import org.apache.jena.rdf.model.Statement;

import java.util.Collection;
import java.util.ResourceBundle;

public class OntologyPathPredicateResolver extends DefaultResponseResolver<Collection<Statement>> {
    public static final String RESULT_KEY_ONTOLOGY_PATH_PREDICATE = "ontologyPathPredicate";

    @Override
    protected void resolveInternal(final Collection<Statement> candidatesForOntologyPathPredicate, final SparqlEndpointTask<?> requestHandler) {
        Platform.runLater(() -> {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
            final RDFNodeChooserDialog dialog = new RDFNodeChooserDialog(candidatesForOntologyPathPredicate,
                                                                         RDFNodeChooserDialog.IS_DBPEDIA_SITE,
                                                                         resourceBundle.getString("ontology-path-predicate-dialog-title"),
                                                                         resourceBundle.getString("ontology-path-predicate-dialog-header")
            );
            dialog.showDialogueAndWait(stmt -> result.addMetadata(RESULT_KEY_ONTOLOGY_PATH_PREDICATE, stmt.getPredicate()));

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread otherwise waits for another 5 seconds
            markResponseReady();
            requestHandler.unlockDialogPane();
        });
    }


}
