package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeReferenceHolder;
import cz.zcu.jsmahy.datamining.api.DefaultAmbiguousInputResolver;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Collection;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DefaultAllAmbiguousInputResolver<T extends RDFNode> implements DefaultAmbiguousInputResolver<T, Void> {

    @Override
    public DataNodeReferenceHolder<T> resolveRequest(final ObservableList<DataNode<T>> dataNodeList, final RequestHandler<T, Void> requestHandler,
                                                     final Property ontologyPathPredicate,
                                                     final Collection<Restriction> restrictions, final Model model) {
        return new DataNodeReferenceHolder<>();
    }
}
