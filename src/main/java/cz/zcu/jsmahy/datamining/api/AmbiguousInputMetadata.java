package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.Restriction;
import lombok.Data;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;

import java.util.Collection;

@Data
public class AmbiguousInputMetadata<T, R> {
    private final RequestHandler<T, R> requestHandler;
    private final Property ontologyPathPredicate;
    private final Collection<Restriction> restrictions;
    private final Model model;
}
