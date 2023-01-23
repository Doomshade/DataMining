package cz.zcu.jsmahy.datamining.api;

import cz.zcu.jsmahy.datamining.query.Restriction;
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
 * mean he hasn't chosen ANYTHING yet. The finished property helps distinguish that.
 * TODO: make it a reference to a collection of data nodes
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DataNodeReferenceHolder<V> {
    private final ObservableList<DataNode<V>> references = FXCollections.observableArrayList();

    private final ObjectProperty<Property> ontologyPathPredicate = new SimpleObjectProperty<>();

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

    public Property getOntologyPathPredicate() {
        return ontologyPathPredicate.get();
    }

    public void setOntologyPathPredicate(final Property ontologyPathPredicate) {
        this.ontologyPathPredicate.set(ontologyPathPredicate);
    }

    public ObjectProperty<Property> ontologyPathPredicateProperty() {
        return ontologyPathPredicate;
    }

    public boolean hasMultipleReferences() {
        return references.size() > 1;
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
