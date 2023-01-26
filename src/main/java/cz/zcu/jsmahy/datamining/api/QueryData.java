package cz.zcu.jsmahy.datamining.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Collection;
import java.util.List;

/**
 * Bean for the query progression. Used in {@link AmbiguousInputResolver}s to help them distinguish what the problem is.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryData {
    /**
     * A collection of restrictions for the search. This could be for example the dynasty the monarch has to be in.
     */
    private Collection<Restriction> restrictions;
    /**
     * The model that gets read to.
     */
    private Model currentModel;
    /**
     * The subject for the very first search.
     */
    private Resource initialSubject;
    /**
     * Candidates for ontology path predicates -- i.e. the path the user wants to search under, such as successor, predecessor, doctoral advisor, ...
     */
    private List<Statement> candidatesForOntologyPathPredicate;
    /**
     * Candidates for start and end dates -- i.e. the time interval.
     */
    private List<Statement> candidatesForStartAndEndDates;
    /**
     * The path the user chose to search under, such as successor, predecessor, doctoral advisor, ...
     */
    private Property ontologyPathPredicate;
    /**
     * The start date the user chose.
     */
    private Property startDateProperty;
    /**
     * The end date the user chose.
     */
    private Property endDateProperty;
}
