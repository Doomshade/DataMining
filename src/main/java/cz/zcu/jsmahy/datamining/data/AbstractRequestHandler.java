package cz.zcu.jsmahy.datamining.data;

import cz.zcu.jsmahy.datamining.exception.InvalidQueryException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public abstract class AbstractRequestHandler extends Service<Ontology> implements IRequestHandler {
    private Request request;

    @Override
    public final Service<Ontology> query(final Request request)
            throws InvalidQueryException {
        this.request = request;
        return this;
    }

    @Override
    protected Task<Ontology> createTask() {
        return new Task<>() {
            @Override
            protected Ontology call() {
                return query0(request);
            }
        };
    }


    protected abstract Ontology query0(final Request request)
            throws InvalidQueryException;
}
