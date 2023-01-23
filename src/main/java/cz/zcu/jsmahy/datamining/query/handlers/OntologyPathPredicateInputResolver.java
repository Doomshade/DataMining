package cz.zcu.jsmahy.datamining.query.handlers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import cz.zcu.jsmahy.datamining.api.*;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

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
        private static final Cache<String, String> modelCache = CacheBuilder.newBuilder()
                                                                            .maximumWeight(5_242_880) // 5 MiB
                                                                            .weigher((Weigher<String, String>) (key, value) -> key.length() + value.length())
                                                                            .concurrencyLevel(20)
                                                                            .build();
        private final Dialog<Property> dialog = new Dialog<>();
        private final TableView<Statement> content;
        private final DataNodeReferenceHolder<T> ref;
        private final Object lock = new Object();
        @SuppressWarnings("unchecked")
        private final ObservableSet<Service<String>> services = FXCollections.synchronizedObservableSet(FXCollections.observableSet());

        private DialogWrapper(final DataNodeReferenceHolder<T> ref, final StmtIterator candidateOntologyPathPredicates) {
            this.ref = ref;
            this.content = new TableView<>();
            this.content.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            this.content.getItems()
                        .addAll(candidateOntologyPathPredicates.toList());
            final TableColumn<Statement, String> propertyColumn = new TableColumn<>();
            propertyColumn.setCellValueFactory(this::cellValueFactoryCallback);

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
            final DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getButtonTypes()
                      .addAll(ButtonType.OK, ButtonType.CANCEL);
            dialogPane.setContent(content);

            // TODO: progress indicator is not shown
            dialogPane.disableProperty()
                      .bind(Bindings.size(services)
                                    .greaterThan(0));
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


        private ObservableValue<String> cellValueFactoryCallback(TableColumn.CellDataFeatures<Statement, String> features) {
            final Property predicate = features.getValue()
                                               .getPredicate();
            final String uri = predicate.getURI();
            if (!uri.contains("dbpedia")) {
                return null;
            }
            final ReadOnlyObjectWrapper<String> observableValue = new ReadOnlyObjectWrapper<>();
            final String cachedItem = modelCache.getIfPresent(uri);
            if (cachedItem != null) {
                observableValue.set(cachedItem);
                return observableValue;
            }
            final Service<String> bgService = new Service<>() {
                @Override
                protected Task<String> createTask() {
                    return new Task<>() {
                        @Override
                        protected String call() {
                            synchronized (lock) {
                                final String cachedValue = modelCache.getIfPresent(uri);
                                if (cachedValue != null) {
                                    return cachedValue;
                                }
                                modelCache.put(uri, "");
                            }
                            final Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
                            try {
                                model.read(uri);
                            } catch (Exception e) {
                                LOGGER.throwing(e);
                                return null;
                            }
                            final Property labelProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#", "label");
                            final Statement val = model.getProperty(predicate, labelProperty, "en");
                            if (val == null) {
                                LOGGER.info("Failed to find " + uri);
                                return null;
                            }
                            final String str = val.getString();
                            modelCache.put(uri, str);
                            LOGGER.info("URI: {}, STR: {}", uri, str);
                            return str;
                        }
                    };
                }
            };
            bgService.setOnSucceeded(e -> {
                services.remove(bgService);
                final String value = (String) e.getSource()
                                               .getValue();
                observableValue.setValue(value);
                final TableView<Statement> tv = features.getTableView();
                tv.refresh();
                // WARN: this could be expensive
                tv.sort();
            });
            bgService.setOnFailed(e -> {
                services.remove(bgService);
                observableValue.setValue("");
            });
            bgService.start();
            services.add(bgService);

            return observableValue;
        }
    }

}
