package cz.zcu.jsmahy.datamining.resolvers;

import cz.zcu.jsmahy.datamining.api.*;
import javafx.application.Platform;

import java.util.List;

public class StartAndEndDateResolver<R extends Void> implements BlockingResponseResolver<R, BlockingDataNodeReferenceHolder> {

    @Override
    public BlockingDataNodeReferenceHolder resolveRequest(final List<DataNode> ambiguousInput, final QueryData inputMetadata, final SparqlEndpointTask<R> requestHandler) {
        final BlockingDataNodeReferenceHolder ref = new BlockingDataNodeReferenceHolder();
        Platform.runLater(() -> {
            final RDFNodeChooserDialog startDateDialog = new RDFNodeChooserDialog(inputMetadata.getCandidatesForStartAndEndDates(), RDFNodeChooserDialog.IS_DBPEDIA_SITE);
            startDateDialog.showDialogueAndWait(statement -> ref.setStartDatePredicate(statement.getPredicate()));

            final RDFNodeChooserDialog endDateDialog = new RDFNodeChooserDialog(inputMetadata.getCandidatesForStartAndEndDates(), RDFNodeChooserDialog.IS_DBPEDIA_SITE);
            endDateDialog.showDialogueAndWait(statement -> ref.setEndDatePredicate(statement.getPredicate()));

            // once we receive the response notify the thread under the request handler's monitor
            // that we got a response from the user
            // the thread waits otherwise for another 5 seconds
            ref.finish();
            requestHandler.unlockDialogPane();
        });
        return ref;
    }
}
