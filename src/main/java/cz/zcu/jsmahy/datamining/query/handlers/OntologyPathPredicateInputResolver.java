package cz.zcu.jsmahy.datamining.query.handlers;

import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class OntologyPathPredicateInputResolver<T, R> implements BlockingAmbiguousInputResolver<T, R> {
    private static final Logger LOGGER = LogManager.getLogger(OntologyPathPredicateInputResolver.class);

    @Override
    public BlockingDataNodeReferenceHolder<T> resolveRequest(final List<DataNode<T>> ambiguousInput, final QueryData inputMetadata, final RequestHandler<T, R> requestHandler) {
        final BlockingDataNodeReferenceHolder<T> ref = new BlockingDataNodeReferenceHolder<>();

        Platform.runLater(() -> {
            final DialogWrapper dialog = new DialogWrapper(ref, inputMetadata.getCandidateOntologyPathPredicates());
            dialog.showDialogueAndWait(requestHandler);
        });
        return ref;
    }


    private class DialogWrapper {
        private static final Model EMPTY_MODEL = ModelFactory.createDefaultModel();

        private final Dialog<Property> dialog = new Dialog<>();
        private final TableView<Statement> content;
        private final DataNodeReferenceHolder<T> ref;
        private final Map<String, Model> modelCache = new HashMap<>();

        private DialogWrapper(final DataNodeReferenceHolder<T> ref, final StmtIterator candidateOntologyPathPredicates) {
            this.ref = ref;
            this.content = new TableView<>();
            this.content.getItems()
                        .addAll(candidateOntologyPathPredicates.toList());
            final TableColumn<Statement, String> propertyColumn = new TableColumn<>();

            // FIXME: this runs on FX UI thread, perhaps try to find a workaround for that someday
            propertyColumn.setCellValueFactory(features -> {
                final Property predicate = features.getValue()
                                                   .getPredicate();
                final String uri = predicate.getURI();
                if (!uri.contains("dbpedia")) {
                    return null;
                }
                addModel(uri);

                if (!modelCache.containsKey(uri)) {
                    modelCache.put(uri, EMPTY_MODEL);
                }

                final Model propertyModel = modelCache.get(uri);
                if (propertyModel == EMPTY_MODEL) {
                    return null;
                }
                final Property labelProperty = propertyModel.getProperty("http://www.w3.org/2000/01/rdf-schema#", "label");
                final Statement val = propertyModel.getProperty(predicate, labelProperty, "en");
                if (val == null) {
                    LOGGER.info("Failed to find " + uri);
                    modelCache.put(uri, EMPTY_MODEL);
                    return null;
                }
                return new ReadOnlyObjectWrapper<>(val.getObject()
                                                      .asLiteral()
                                                      .getString());
            });

            final TableColumn<Statement, String> valueColumn = new TableColumn<>();
            valueColumn.setCellValueFactory(features -> new ReadOnlyObjectWrapper<>(features.getValue()
                                                                                            .getObject()
                                                                                            .toString()));
            final ObservableList<TableColumn<Statement, ?>> columns = this.content.getColumns();
            columns.add(propertyColumn);
            columns.add(valueColumn);

            this.dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.CANCEL) {
                    return null;
                }
                if (buttonType == ButtonType.OK) {
                    return content.getSelectionModel()
                                  .getSelectedItem()
                                  .getPredicate();
                }
                LOGGER.error("Unrecognized button type: {}", buttonType);
                return null;
            });
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getButtonTypes()
                      .addAll(ButtonType.OK, ButtonType.CANCEL);
            dialogPane.setContent(content);
        }

        private void addModel(final String uri) {
            if (modelCache.containsKey(uri)) {
                return;
            }
            Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            try {
                model.read(uri);
            } catch (Exception e) {
                LOGGER.throwing(e);
                model = EMPTY_MODEL;
            }
            modelCache.putIfAbsent(uri, model);
        }


        public void showDialogueAndWait(final RequestHandler<T, R> requestHandler) {
            try {
                final Optional<Property> property = dialog.showAndWait();
                property.ifPresent(ref::setOntologyPathPredicate);
            } catch (Exception e) {
                LOGGER.throwing(e);
            }
            if (ref instanceof BlockingDataNodeReferenceHolder<T> blockingRef) {
                blockingRef.unlock();
                requestHandler.unlockDialogPane();
            }
        }
    }

}
