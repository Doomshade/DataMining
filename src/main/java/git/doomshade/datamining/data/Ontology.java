package git.doomshade.datamining.data;

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

    private static int ID_INC = 0;

    // adjacency list
    private final List<List<Integer>> ADJ_LIST = new ArrayList<>();
    private final List<Link> LINKS = new ArrayList<>();

    public Ontology(RDFNode start) {
        LINKS.add(new Link(start));
    }

    public Link getStart() {
        return LINKS.get(0);
    }

    public void printOntology(PrintStream out) {
        out.println("Printing ontology");

        for (int i = 0; i < ADJ_LIST.size(); i++) {
            System.out.printf("Info: %s%n", LINKS.get(i).getInfo());
            System.out.printf("%s", LINKS.get(i));
            for (int j : ADJ_LIST.get(i)) {
                System.out.printf(" -> %s", LINKS.get(j));
            }
            System.out.println();
        }
    }

    /**
     * Adds an oriented edge to the graph
     *
     * @param from the first point
     * @param to   the second point
     */
    public void addEdge(Link from, Link to) {
        // expand the links dynamically
        expand(from.id + 1);

        // this should not happen, but let's make sure it's all g
        if (ADJ_LIST.size() < from.id) {
            throw new IllegalStateException("Failed to allocate memory for links");
        }

        // add the link IDs to the list
        ADJ_LIST.get(from.id).add(to.id);
    }

    private void expand(int size) {
        for (int i = 0; i < size - ADJ_LIST.size(); i++) {
            ADJ_LIST.add(new ArrayList<>());
        }
    }

    /**
     * Creates a link and registers its ID in memory
     *
     * @param node the node to create the link for
     * @return a link with corresponding node
     */
    public Link createLink(RDFNode node) {
        Link link = new Link(node);
        LINKS.add(link);
        return link;
    }

    public static final class Link {
        private final RDFNode node;
        private final int id = ID_INC++;

        private Link(RDFNode node) {
            Objects.requireNonNull(node, "Node cannot be null!");
            this.node = node;
        }

        @Override
        public String toString() {
            return "Link{" +
                    "node=" + node +
                    ", id=" + id +
                    '}';
        }

        public String getInfo(){
            if (!node.isResource()){
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
