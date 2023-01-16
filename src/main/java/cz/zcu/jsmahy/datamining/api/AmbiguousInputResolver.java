package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.Restriction;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;

import java.util.Collection;

public interface AmbiguousInputResolver<T, R, DNRef extends DataNodeReferenceHolder<T>> {
    DNRef resolveRequest(ObservableList<DataNode<T>> param, final RequestHandler<T, R> requestHandler, final Property ontologyPathPredicate,
                         final Collection<Restriction> restrictions,
                         final Model model);
}
