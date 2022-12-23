package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.AmbiguitySolver;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import javafx.scene.control.TreeItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;


@Data
@AllArgsConstructor
public final class SparqlRequest<T, R> {
    /**
     * the request to send to the web page, e.g. {@code Windows_10}
     */
    @NonNull
    private final String requestPage;
    /**
     * the namespace, e.g. <a href="https://dbpedia.org/property/">a property</a> or <a href="https://dbpedia.org/ontology/">an ontology</a>
     */
    @NonNull
    private final String namespace;
    /**
     * the link to create the ontology for, e.g. {@code precededBy}
     */
    @NonNull
    private final String link;
    /**
     * the tree root to add nodes to
     */
    @NonNull
    private final TreeItem<T> treeRoot;

    /**
     * The data node root to append the nodes to.
     */
    @NonNull
    private final DataNodeRoot<T> dataNodeRoot;

    @NonNull
    private final AmbiguitySolver<T, R> ambiguitySolver;

    /**
     * The restrictions (or rules) of this request. Used for filtering responses to the SPARQL request.
     */
    private final Collection<Restriction> restrictions = new LinkedList<>();

    /**
     * Adds a restriction to the request
     *
     * @param restriction the restriction
     */
    public void addRestriction(Restriction restriction) {
        this.restrictions.add(restriction);
    }

    /**
     * Adds restrictions to the request
     *
     * @param restrictions the restrictions
     */
    public void addRestrictions(Iterable<? extends Restriction> restrictions) {
        restrictions.forEach(this::addRestriction);
    }

    public Collection<Restriction> getRestrictions() {
        return Collections.unmodifiableCollection(restrictions);
    }
}
