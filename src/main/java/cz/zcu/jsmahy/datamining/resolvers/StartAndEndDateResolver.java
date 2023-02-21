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
import java.util.ResourceBundle;

public class StartAndEndDateResolver extends DefaultResponseResolver<Collection<Statement>> {

    public static final String RESULT_KEY_START_DATE_PREDICATE = "startDatePredicate";
    public static final String RESULT_KEY_END_DATE_PREDICATE = "endDatePredicate";

    @Override
    protected void resolveInternal(final Collection<Statement> candidatesForStartAndEndDates, final SparqlEndpointTask<?> requestHandler) {
        Platform.runLater(() -> {
            final Callback<TableColumn.CellDataFeatures<Statement, String>, ObservableValue<String>> cellValueCallback = features -> {
                final Object date = features.getValue()
                                            .getObject()
                                            .asLiteral()
                                            .getValue();
                return new ReadOnlyObjectWrapper<>(date.toString());
            };
            // TODO: this should be separate
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("lang");
            final RDFNodeChooserDialog startDateDialog = new RDFNodeChooserDialog(candidatesForStartAndEndDates,
                                                                                  RDFNodeChooserDialog.IS_DBPEDIA_SITE,
                                                                                  resourceBundle.getString("start-date-dialog-title"),
                                                                                  resourceBundle.getString("start-date-dialog-header")
            );
            startDateDialog.showDialogueAndWait(statement -> result.addMetadata(RESULT_KEY_START_DATE_PREDICATE, statement.getPredicate()));

            final RDFNodeChooserDialog endDateDialog = new RDFNodeChooserDialog(candidatesForStartAndEndDates,
                                                                                RDFNodeChooserDialog.IS_DBPEDIA_SITE,
                                                                                resourceBundle.getString("end-date-dialog-title"),
                                                                                resourceBundle.getString("end-date-dialog-header")
            );
            endDateDialog.showDialogueAndWait(statement -> result.addMetadata(RESULT_KEY_END_DATE_PREDICATE, statement.getPredicate()));

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            markResponseReady();
            requestHandler.unlockDialogPane();
        });
    }
}
