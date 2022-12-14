package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import javafx.scene.control.TreeItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.LinkedList;


@Data
@AllArgsConstructor
public final class SparqlRequest<T> {
    /**
     * the request to send to the web page, e.g. {@code Windows_10}
     */
    private final String requestPage;
    /**
     * the namespace, e.g. <a href="https://dbpedia.org/property/">a property</a> or <a href="https://dbpedia.org/ontology/">an ontology</a>
     */
    private final String namespace;
    /**
     * the link to create the ontology for, e.g. {@code precededBy}
     */
    private final String link;
    /**
     * the tree root to add nodes to
     */
    private final TreeItem<T> root;

    private final DataNodeRoot<T> dataNodeRoot;

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

}
