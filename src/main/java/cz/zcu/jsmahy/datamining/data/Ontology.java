package cz.zcu.jsmahy.datamining.data;

import org.apache.jena.rdf.model.RDFNode;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class Ontology {

    private int idCounter = 0;

    // adjacency list
    private final List<List<Integer>> ADJ_LIST = new ArrayList<>();
    private final List<Link> LINKS = new ArrayList<>();

    public Ontology(RDFNode start) {
        LINKS.add(new Link(start, idCounter++));
    }

    public Link getStart() {
        return LINKS.get(0);
    }

    public void printOntology(PrintStream out) {
        out.println("Printing ontology");

        for (int i = 0; i < ADJ_LIST.size(); i++) {
            //System.out.printf("Info: %s%n", LINKS.get(i).getInfo());
            System.out.printf("%s", LINKS.get(i));
            for (int j : ADJ_LIST.get(i)) {
                System.out.printf(" -> %s", LINKS.get(j));
            }
            System.out.println();
        }
    }

    public void printOntology(StringBuilder sb) {
        for (int i = 0; i < ADJ_LIST.size(); i++) {
            sb.append(LINKS.get(i));
            for (int j : ADJ_LIST.get(i)) {
                sb.append(" -> ").append(LINKS.get(j));
            }
            sb.append("\n");
        }
    }

    /**
     * Adds an oriented edge to the graph
     *
     * @param from the first point
     * @param to   the second point
     */
    public synchronized void addEdge(Link from, Link to) {
        // expand the links dynamically
        expand(from.id);

        // this should not happen, but let's make sure it's all g
        if (from.id >= ADJ_LIST.size()) {
            throw new IllegalStateException(
                    String.format("Failed to allocate memory for links (from = %d, list size = %d)", from.id,
                            ADJ_LIST.size()),
                    new IndexOutOfBoundsException());
        }

        // add the link IDs to the list
        ADJ_LIST.get(from.id).add(to.id);
    }

    private synchronized void expand(int size) {
        while (ADJ_LIST.size() <= size) {
            ADJ_LIST.add(new ArrayList<>());
        }
    }

    /**
     * Creates a link and registers its ID in memory
     *
     * @param node the node to create the link for
     *
     * @return a link with corresponding node
     */
    public Link createLink(RDFNode node) {
        Link link = new Link(node, idCounter++);
        LINKS.add(link);
        return link;
    }

    public static final class Link {
        private final RDFNode node;
        private final int id;

        private Link(RDFNode node, int id) {
            Objects.requireNonNull(node, "Node cannot be null!");
            this.id = id;
            this.node = node;
        }

        @Override
        public String toString() {
            return "Link{" +
                    "node=" + node +
                    ", id=" + id +
                    '}';
        }

        public String getInfo() {
            if (!node.isResource()) {
                return "";
            }
            return node.asResource().listProperties().toList().toString();
            //return node.asResource().getProperty(new PropertyImpl("https://dbpedia.org/ontology/abstract")).getObject().toString();
        }

        public RDFNode getNode() {
            return node;
        }
    }
}
