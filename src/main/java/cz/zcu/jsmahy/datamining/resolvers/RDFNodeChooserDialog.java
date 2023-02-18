package cz.zcu.jsmahy.datamining.resolvers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import cz.zcu.jsmahy.datamining.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

class RDFNodeChooserDialog {
    public static final Predicate<String> IS_DBPEDIA_SITE = uri -> uri.contains("dbpedia");

    private static final Logger LOGGER = LogManager.getLogger(RDFNodeChooserDialog.class);
    private static final Cache<String, String> modelCache = CacheBuilder.newBuilder()
                                                                        .maximumWeight(5_242_880) // 5 MiB
                                                                        .weigher((Weigher<String, String>) (key, value) -> key.length() + value.length())
                                                                        .concurrencyLevel(20)
                                                                        .build();
    private final Dialog<Statement> dialog = new Dialog<>();
    private final TableView<Statement> content;
    private final Object lock = new Object();
    private final Predicate<String> uriPredicate;

    @SuppressWarnings("unchecked")
    private final ObservableSet<Service<String>> services = FXCollections.synchronizedObservableSet(FXCollections.observableSet());

    /**
     * @param statements              The statements to display
     * @param uriPredicate            The predicate for the given URI in each cell. The predicate gets called for each cell in the {@code propertyColumn} (the first column) and if
     *                                {@link Predicate#test(Object)} returns {@code true} it will attempt to look for the label of the property.
     * @param title
     * @param headerText
     * @param valueColumnValueFactory the value column cell value factory
     */
    RDFNodeChooserDialog(final Collection<Statement> statements,
                         final Predicate<String> uriPredicate,
                         final String title,
                         final String headerText,
                         final Callback<TableColumn.CellDataFeatures<Statement, String>, ObservableValue<String>> valueColumnValueFactory) {
        this(statements, uriPredicate, title, headerText, valueColumnValueFactory, null);
    }

    /**
     * @param statements              The statements to display
     * @param uriPredicate            The predicate for the given URI in each cell. The predicate gets called for each cell in the {@code propertyColumn} (the first column) and if
     *                                {@link Predicate#test(Object)} returns {@code true} it will attempt to look for the label of the property.
     * @param title
     * @param headerText
     */
    RDFNodeChooserDialog(final Collection<Statement> statements,
                         final Predicate<String> uriPredicate,
                         final String title,
                         final String headerText,
                         final Callback<TableColumn.CellDataFeatures<Statement, String>, ObservableValue<String>> valueColumnValueFactory,
                         final Callback<TableColumn<Statement, String>, TableCell<Statement, String>> valueColumnCellFactory) {
        this.uriPredicate = uriPredicate;
        this.content = new TableView<>();
        this.content.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.content.getItems()
                    .addAll(statements);
        this.dialog.initOwner(Main.getPrimaryStage());
        this.dialog.setResizable(true);
        this.dialog.setTitle(title);
        this.dialog.setHeaderText(headerText);
        // TODO: perhaps add tooltip with the URI to the property someday :)
        final TableColumn<Statement, String> propertyColumn = new TableColumn<>("Přísudek");
        propertyColumn.setCellValueFactory(features -> cellValueFactoryCallback(features, features.getValue()
                                                                                                  .getPredicate()));

        final TableColumn<Statement, String> valueColumn = new TableColumn<>("Předmět");
        valueColumn.setCellValueFactory(features -> cellValueFactoryCallback(features, features.getValue()
                                                                                               .getObject()));
        if (valueColumnCellFactory != null) {
            valueColumn.setCellFactory(valueColumnCellFactory);
        }

        final ObservableList<TableColumn<Statement, ?>> columns = this.content.getColumns();
        columns.add(propertyColumn);
        columns.add(valueColumn);

        this.dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.CANCEL) {
                return null;
            }
            if (buttonType == ButtonType.OK) {
                return content.getSelectionModel()
                              .getSelectedItem();
            }
            LOGGER.error("Unrecognized button type: {}", buttonType);
            return null;
        });
        final DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes()
                  .addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.setContent(content);
        dialogPane.disableProperty()
                  .bind(Bindings.size(services)
                                .greaterThan(0));
    }

    public void showDialogueAndWait(final Consumer<Statement> chosenStatementConsumer) {
        try {
            final Optional<Statement> statement = dialog.showAndWait();
            statement.ifPresent(chosenStatementConsumer);
        } catch (Exception e) {
            LOGGER.throwing(e);
        }
    }


    private ObservableValue<String> cellValueFactoryCallback(TableColumn.CellDataFeatures<Statement, String> features, final RDFNode rdfNode) {
        final ReadOnlyObjectWrapper<String> observableValue = new ReadOnlyObjectWrapper<>();
        if (!rdfNode.isURIResource()) {
            if (!rdfNode.isLiteral()) {
                observableValue.set(rdfNode.toString());
            } else {
                observableValue.set(rdfNode.asLiteral()
                                           .getValue()
                                           .toString());
            }
            return observableValue;
        }
        final Resource resource = rdfNode.asResource();
        final String uri = resource.getURI();
        if (!uriPredicate.test(uri)) {
            observableValue.set(uri);
            return observableValue;
        }

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

                        final Model model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
                        try {
                            model.read(uri);
                        } catch (Exception e) {
                            LOGGER.throwing(e);
                            return null;
                        }
                        final Property labelProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#", "label");
                        final Statement val = model.getProperty(resource, labelProperty, "en");
                        if (val == null) {
                            LOGGER.debug("Failed to find " + uri);
                            return uri;
                        }

                        final String str = val.getString();
                        modelCache.put(uri, str);
                        LOGGER.trace("URI: {}, STR: {}", uri, str);
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
            // this is there to load all the values
            tv.sort();
        });

        bgService.setOnFailed(e -> {
            services.remove(bgService);
            observableValue.setValue("");
            LOGGER.throwing(bgService.getException());
        });

        bgService.start();
        services.add(bgService);

        return observableValue;
    }
}
