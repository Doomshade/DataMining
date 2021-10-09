package git.doomshade.datamining.data;

import git.doomshade.datamining.data.exception.InvalidQueryException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public abstract class AbstractRequestHandler extends Service<Ontology> implements IRequestHandler {
    private String request, namespace, link;

    @Override
    public final Service<Ontology> query(final String request, final String namespace, final String link)
            throws InvalidQueryException {
        this.request = request;
        this.namespace = namespace;
        this.link = link;
        return this;
    }

    @Override
    protected Task<Ontology> createTask() {
        return new Task<>() {
            @Override
            protected Ontology call() {
                return query0(request, namespace, link);
            }
        };
    }



    protected abstract Ontology query0(final String request, final String namespace, final String link)
            throws InvalidQueryException;
}
