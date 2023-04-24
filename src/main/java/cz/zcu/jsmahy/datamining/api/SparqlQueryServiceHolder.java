package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.app.controller.MainController;
import javafx.concurrent.Service;

/**
 * A class that binds to a sparql endpoint.
 *
 * @author Jakub Šmrha
 * @see MainController
 * @since 1.0
 */
public interface SparqlQueryServiceHolder {
    void bindQueryService(Service<?> service);
}
