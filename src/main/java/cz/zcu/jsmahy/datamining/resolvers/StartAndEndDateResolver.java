package cz.zcu.jsmahy.datamining.resolvers;

import cz.zcu.jsmahy.datamining.api.DefaultResponseResolver;
import cz.zcu.jsmahy.datamining.api.SparqlEndpointTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.jena.rdf.model.Statement;

import java.util.Collection;

public class StartAndEndDateResolver extends DefaultResponseResolver<Collection<Statement>> {

    public static final String RESULT_KEY_START_DATE_PREDICATE = "startDatePredicate";
    public static final String RESULT_KEY_END_DATE_PREDICATE = "endDatePredicate";

    @Override
    public void resolve(final Collection<Statement> candidatesForStartAndEndDates, final SparqlEndpointTask<?> requestHandler) {
        Platform.runLater(() -> {
            final Callback<TableColumn.CellDataFeatures<Statement, String>, ObservableValue<String>> cellValueCallback = features -> {
                final Object date = features.getValue()
                                            .getObject()
                                            .asLiteral()
                                            .getValue();
                return new ReadOnlyObjectWrapper<>(date.toString());
            };
            final RDFNodeChooserDialog startDateDialog = new RDFNodeChooserDialog(candidatesForStartAndEndDates, RDFNodeChooserDialog.IS_DBPEDIA_SITE, cellValueCallback);
            startDateDialog.showDialogueAndWait(statement -> result.addMetadata(RESULT_KEY_START_DATE_PREDICATE, statement.getPredicate()));

            final RDFNodeChooserDialog endDateDialog = new RDFNodeChooserDialog(candidatesForStartAndEndDates, RDFNodeChooserDialog.IS_DBPEDIA_SITE, cellValueCallback);
            endDateDialog.showDialogueAndWait(statement -> result.addMetadata(RESULT_KEY_END_DATE_PREDICATE, statement.getPredicate()));

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            markResponseReady();
            requestHandler.unlockDialogPane();
        });
    }
}
