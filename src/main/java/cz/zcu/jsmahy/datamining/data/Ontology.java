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

    // adjacency list
    private final List<List<Integer>> adjList = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();
    private int idCounter = 0;

    public Ontology(RDFNode start) {
        links.add(new Link(start, idCounter++));
    }

    public Link getStart() {
        if (links.isEmpty()) {
            // TODO log something or throw an exception maybe?
            return null;
        }
        return links.get(0);
    }

    public void printOntology(PrintStream out) {
        out.println("Printing ontology");

        for (int i = 0; i < adjList.size(); i++) {
            System.out.printf("%s", links.get(i));
            for (int j : adjList.get(i)) {
                System.out.printf(" -> %s", links.get(j));
            }
            System.out.println();
        }
    }

    public void printOntology(StringBuilder sb) {
        for (int i = 0; i < adjList.size(); i++) {
            sb.append(links.get(i));
            for (int j : adjList.get(i)) {
                sb.append(" -> ").append(links.get(j));
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
        if (from.id >= adjList.size()) {
            throw new IllegalStateException(
                    String.format("Failed to allocate memory for links (from = %d, list size = %d)", from.id,
                            adjList.size()),
                    new IndexOutOfBoundsException());
        }

        // add the link IDs to the list
        adjList.get(from.id).add(to.id);
    }

    private synchronized void expand(int size) {
        while (adjList.size() <= size) {
            adjList.add(new ArrayList<>());
        }
    }

    /**
     * Creates a link and registers its ID in memory
     *
     * @param node the node to create the link for
     * @return a link with corresponding node
     */
    public Link createLink(final RDFNode node) {
        final Link link = new Link(node, idCounter++);
        links.add(link);
        return link;
    }

    public static final class Link {
        private final RDFNode node;
        private final int id;

        private Link(final RDFNode node, final int id) {
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
