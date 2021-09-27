package git.doomshade.datamining.data;

import org.apache.jena.rdf.model.RDFNode;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class Ontology {
    public static final int ROOT_DEPTH = 0;
    private final Link root;
    private final List<Collection<Link>> LINKS = new LinkedList<>();

    public Ontology(RDFNode root) {
        this.root = new Link(root);
        LINKS.add(Collections.singletonList(this.root));
    }

    public Link getRoot() {
        return root;
    }

    public void printOntology(PrintStream out) {
        out.println("Printing ontology");

        for (Collection<Link> deepestLinks : LINKS) {
            for (Link link : deepestLinks) {
                out.println(link);
            }
        }
    }

    public final class Link {
        private final RDFNode node;
        private Link parent = null;

        public Link(RDFNode node) {
            this.node = node;
        }

        /**
         * @return the parent of this link or {@code null} if no parent is present :((
         */
        public Link getParent() {
            return parent;
        }

        /**
         * Adds a child link to this one
         *
         * @param link the link to add
         */
        public void addChild(Link link, int depth) {
            if (link == null) {
                return;
            }
            // set the child's depth to this + 1 and the parent to this
            link.parent = this;

            // if this is a new deepest link create a new collection of links and add it to the depth links
            if (LINKS.size() <= depth) {
                Collection<Link> links = new LinkedList<>();
                links.add(link);
                LINKS.add(links);
            }
            // else add the link to its corresponding depth in the LINKS list
            else {
                LINKS.get(depth).add(link);
            }
        }

        @Override
        public String toString() {
            return "Link{" +
                    "node=" + node +
                    (parent != null ? ", parent=" + parent.getNode() : "") +
                    '}';
        }

        public RDFNode getNode() {
            return node;
        }
    }
}
