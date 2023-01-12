package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.query.Restriction;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.jena.rdf.model.Property;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@link DataNode} reference with convenience methods to distinguish various outputs. The main reason being the output being {@code null} because the user chose nothing -- that doesn't necessarily
 * mean he hasn't chosen ANYTHING yet. The finished property helps distinguish that.
 * TODO: make it a reference to a collection of data nodes
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DataNodeReferenceHolder<V> {
    private final ObservableList<DataNode<V>> references = FXCollections.observableArrayList();

    private final ReadOnlyBooleanWrapper hasMultipleReferences = new ReadOnlyBooleanWrapper(false);
    /**
     * If true then the reference has been set. This exists because the default value of reference is {@code null}, and when setting null the request handler has no way of telling whether we actually
     * set it because it would check for the default state -- and that being null. This is a workaround for that issue.
     */
    private final ReadOnlyBooleanWrapper finished = new ReadOnlyBooleanWrapper(false);

    private final ObjectProperty<Property> ontologyPathPredicate = new SimpleObjectProperty<>();

    private final ListProperty<Restriction> restrictions = new SimpleListProperty<>();

    public ObservableList<Restriction> getRestrictions() {
        return restrictions.get();
    }

    public ListProperty<Restriction> restrictionsProperty() {
        return restrictions;
    }

    public void setRestrictions(final ObservableList<Restriction> restrictions) {
        this.restrictions.set(restrictions);
    }

    public Property getOntologyPathPredicate() {
        return ontologyPathPredicate.get();
    }

    public ObjectProperty<Property> ontologyPathPredicateProperty() {
        return ontologyPathPredicate;
    }

    public void setOntologyPathPredicate(final Property ontologyPathPredicate) {
        this.ontologyPathPredicate.set(ontologyPathPredicate);
    }

    public DataNodeReferenceHolder() {
        this.hasMultipleReferences.bind(Bindings.greaterThanOrEqual(2, Bindings.createIntegerBinding(references::size)));
    }

    public boolean hasMultipleReferences() {
        return hasMultipleReferences.get();
    }

    public ReadOnlyBooleanProperty hasMultipleReferencesProperty() {
        return hasMultipleReferences;
    }

    /**
     * Marks this reference as finished -- the reference has been set.
     */
    public void finish() {
        finished.set(true);
    }

    public boolean isFinished() {
        return finished.get();
    }

    public ReadOnlyBooleanProperty finishedProperty() {
        return finished.getReadOnlyProperty();
    }

    public void set(final DataNode<V> value) {
        this.references.clear();
        this.add(value);
    }

    public void set(final Collection<DataNode<V>> value) {
        this.references.clear();
        this.add(value);
    }

    public void add(final DataNode<V> value) {
        this.references.add(value);
    }

    public void add(final Collection<DataNode<V>> value) {
        this.references.addAll(value);
    }

    public DataNode<V> get() {
        if (references.size() == 0) {
            return null;
        }
        if (references.size() == 1) {
            return references.get(0);
        }
        throw new IllegalStateException("This reference holder contains multiple references.");
    }

    public List<DataNode<V>> getList() {
        return Collections.unmodifiableList(references);
    }
}
