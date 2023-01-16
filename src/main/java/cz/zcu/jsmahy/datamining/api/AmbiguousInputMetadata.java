package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.query.RequestHandler;
import cz.zcu.jsmahy.datamining.query.Restriction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmbiguousInputMetadata<T, R> {
    private RequestHandler<T, R> requestHandler;
    private Property ontologyPathPredicate;
    private Collection<Restriction> restrictions;
    private Model model;
}
