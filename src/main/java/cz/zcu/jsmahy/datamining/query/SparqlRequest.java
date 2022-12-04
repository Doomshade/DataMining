package cz.zcu.jsmahy.datamining.query;

import javafx.scene.control.TreeItem;
import lombok.Data;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Collection;
import java.util.LinkedList;


@Data
public final class SparqlRequest {
    private final String requestPage, namespace, link;
    private final TreeItem<RDFNode> root;
    private final Collection<Restriction> restrictions = new LinkedList<>();

    /**
     * @param requestPage the request to send to the web page, e.g. {@code Windows_10}
     * @param namespace   the namespace, e.g. <a href="https://dbpedia.org/property/">a property</a> or <a
     *                    href="https://dbpedia.org/ontology/">an ontology</a>
     * @param link        the link to create the ontology for, e.g. {@code precededBy}
     * @param root        the tree root to add nodes to
     */
    public SparqlRequest(String requestPage, String namespace, String link, TreeItem<RDFNode> root) {
        this.requestPage = requestPage;
        this.namespace = namespace;
        this.link = link;
        this.root = root;
    }

    public SparqlRequest(String requestPage, String namespace, String link) {
        this(requestPage, namespace, link, null);
    }


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
