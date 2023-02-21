package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Service;

public interface SparqlQueryServiceHolder {
    void bindQueryService(Service<?> service);
}
