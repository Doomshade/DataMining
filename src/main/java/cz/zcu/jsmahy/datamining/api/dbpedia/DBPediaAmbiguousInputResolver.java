package cz.zcu.jsmahy.datamining.api.dbpedia;

import cz.zcu.jsmahy.datamining.api.AmbiguousInputResolver;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.Restriction;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Collection;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public interface DBPediaAmbiguousInputResolver<T extends RDFNode, R> extends AmbiguousInputResolver<T, R> {
    @Override
    DataNodeReferenceHolder<T> call(ObservableList<DataNode<T>> param, final RequestHandler<T, R> requestHandler, final Property ontologyPathPredicate, final Collection<Restriction> restrictions,
                                    final Model model);
}
