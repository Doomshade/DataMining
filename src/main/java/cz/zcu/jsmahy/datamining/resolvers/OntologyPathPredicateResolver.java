package cz.zcu.jsmahy.datamining.resolvers;

import cz.zcu.jsmahy.datamining.api.DefaultResponseResolver;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

import java.util.Collection;
import java.util.ResourceBundle;

public class OntologyPathPredicateResolver extends DefaultResponseResolver<Collection<Statement>> {
    public static final String RESULT_KEY_ONTOLOGY_PATH_PREDICATE = "ontologyPathPredicate";

    @Override
    public void resolve(final Collection<Statement> candidatesForOntologyPathPredicate, final SparqlEndpointTask<?> requestHandler) {
        Platform.runLater(() -> {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
            final RDFNodeChooserDialog dialog = new RDFNodeChooserDialog(candidatesForOntologyPathPredicate,
                                                                         RDFNodeChooserDialog.IS_DBPEDIA_SITE,
                                                                         resourceBundle.getString("ontology-path-predicate-dialog-title"),
                                                                         resourceBundle.getString("ontology-path-predicate-dialog-header"),
                                                                         features -> {
                                                                             final RDFNode object = features.getValue()
                                                                                                            .getObject();
                                                                             assert object.isURIResource(); // should be URI resource because we are looking for a path predicate
                                                                             final String localName = object.asResource()
                                                                                                            .getURI();
                                                                             return new ReadOnlyObjectWrapper<>(localName);
                                                                         });
            dialog.showDialogueAndWait(stmt -> result.addMetadata(RESULT_KEY_ONTOLOGY_PATH_PREDICATE, stmt.getPredicate()));

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            markResponseReady();
            requestHandler.unlockDialogPane();
        });
    }


}
