package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.query.Restriction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryData {
    private Property ontologyPathPredicate;
    private Collection<Restriction> restrictions;
    private Model model;
    private Resource subject;
    private StmtIterator candidateOntologyPathPredicates;
}
