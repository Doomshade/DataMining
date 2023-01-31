package cz.zcu.jsmahy.datamining.api;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.Property;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@link DataNode} reference with convenience methods to distinguish various outputs. The main reason being the output being {@code null} because the user chose nothing -- that doesn't necessarily
 * mean he hasn't chosen ANYTHING yet. The {@code finished} property helps distinguish that.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DataNodeReferenceHolder<V> {
    /**
     * The references.
     */
    private final ObservableList<DataNode> references = FXCollections.observableArrayList();

    /**
     * The ontology path predicate.
     */
    private final ObjectProperty<Property> ontologyPathPredicate = new SimpleObjectProperty<>();

    /**
     * The start date predicate.
     */
    private final ObjectProperty<Property> startDatePredicate = new SimpleObjectProperty<>();
    /**
     * The end date predicate.
     */
    private final ObjectProperty<Property> endDatePredicate = new SimpleObjectProperty<>();

    /**
     * The restrictions.
     */
    private final ListProperty<Restriction> restrictions = new SimpleListProperty<>();

    public ObservableList<Restriction> getRestrictions() {
        return restrictions.get();
    }

    public void setRestrictions(final ObservableList<Restriction> restrictions) {
        this.restrictions.set(restrictions);
    }

    public ListProperty<Restriction> restrictionsProperty() {
        return restrictions;
    }

    public Property getStartDatePredicate() {
        return startDatePredicate.get();
    }

    public void setStartDatePredicate(final Property startDatePredicate) {
        this.startDatePredicate.set(startDatePredicate);
    }

    public ObjectProperty<Property> startDatePredicateProperty() {
        return startDatePredicate;
    }

    public Property getEndDatePredicate() {
        return endDatePredicate.get();
    }

    public void setEndDatePredicate(final Property endDatePredicate) {
        this.endDatePredicate.set(endDatePredicate);
    }

    public ObjectProperty<Property> endDatePredicateProperty() {
        return endDatePredicate;
    }

    public Property getOntologyPathPredicate() {
        return ontologyPathPredicate.get();
    }

    public void setOntologyPathPredicate(final Property ontologyPathPredicate) {
        this.ontologyPathPredicate.set(ontologyPathPredicate);
    }

    public ObjectProperty<Property> ontologyPathPredicateProperty() {
        return ontologyPathPredicate;
    }

    /**
     * @return {@code true} if the reference list size is &gt;1
     */
    public boolean hasMultipleReferences() {
        return references.size() > 1;
    }

    /**
     * Sets the reference
     *
     * @param value the reference
     */
    public void set(final DataNode value) {
        this.references.clear();
        this.add(value);
    }

    /**
     * Sets the references
     *
     * @param value the references
     */
    public void set(final Collection<DataNode> value) {
        this.references.clear();
        this.add(value);
    }

    /**
     * Adds a reference
     *
     * @param value the reference
     */
    public void add(final DataNode value) {
        this.references.add(value);
    }

    /**
     * Adds references
     *
     * @param value the references
     */
    public void add(final Collection<DataNode> value) {
        this.references.addAll(value);
    }

    /**
     * @return the reference or {@code null} if no reference was set
     *
     * @throws IllegalStateException if this reference holder contains multiple references
     */
    public DataNode get() throws IllegalStateException {
        if (references.size() == 0) {
            return null;
        }
        if (references.size() == 1) {
            return references.get(0);
        }
        throw new IllegalStateException("This reference holder contains multiple references.");
    }

    /**
     * @return the list of references
     */
    public List<DataNode> getList() {
        return Collections.unmodifiableList(references);
    }
}
