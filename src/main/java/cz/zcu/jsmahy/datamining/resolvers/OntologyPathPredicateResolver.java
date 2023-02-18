package cz.zcu.jsmahy.datamining.resolvers;

import cz.zcu.jsmahy.datamining.api.DefaultResponseResolver;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.apache.jena.rdf.model.*;

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
                                                                             final Resource resource = object.asResource();
                                                                             final Property labelProperty = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#", "label");
                                                                             String name = resource.getLocalName();
                                                                             // the local name for whatever reason gets cut
                                                                             // if there's a comma in the name
                                                                             // for example Louis IV, Holy Roman Emperor
                                                                             // gets cut to " Holy Roman Emperor"
                                                                             // so just grab the last part of the URI
                                                                             // and hopefully it's ok
                                                                             if (name.isBlank() || name.startsWith("_")) {
                                                                                 final String uri = resource.getURI();
                                                                                 name = uri.substring(uri.lastIndexOf('/'));
                                                                             }
                                                                             name = name.replaceAll("_", " ");
                                                                             return new ReadOnlyObjectWrapper<>(name);
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
