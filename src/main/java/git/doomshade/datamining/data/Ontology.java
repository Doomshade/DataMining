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
    private static final int ROOT_DEPTH = 0;
    private final Link root;
    private final List<Collection<Link>> DEPTH_LINKS = new LinkedList<>();

    private int maxDepth = ROOT_DEPTH;

    public Ontology(RDFNode root) {
        this.root = new Link(root);
        this.root.depth = ROOT_DEPTH;
        DEPTH_LINKS.add(Collections.singletonList(this.root));
    }

    public Link getRoot() {
        return root;
    }

    public void printOntology(PrintStream out) {
        out.println("Printing ontology");

        for (Collection<Link> deepestLinks : DEPTH_LINKS) {
            for (Link link : deepestLinks) {
                out.println(link);
            }
        }
    }

    public final class Link {
        private final List<Link> next = new LinkedList<>();
        private final RDFNode node;
        private Link previous;
        private int depth;

        public Link(RDFNode node) {
            this.node = node;
        }

        public Collection<Link> next() {
            return Collections.unmodifiableCollection(next);
        }

        public int getDepth() {
            return depth;
        }

        public Link getPrevious() {
            return previous;
        }

        public void addLink(Link link) {
            if (link == null) {
                return;
            }
            link.depth = depth + 1;
            link.previous = this;
            if (DEPTH_LINKS.size() <= link.depth) {
                Collection<Link> links = new LinkedList<>();
                links.add(link);
                DEPTH_LINKS.add(links);
            } else {
                DEPTH_LINKS.get(link.depth).add(link);
            }
            maxDepth = Math.max(maxDepth, link.depth);
            next.add(link);
        }

        @Override
        public String toString() {
            return "Link{" +
                    "node=" + node +
                    (previous != null ? ", prev=" + previous.getNode() : "") +
                    ", depth=" + depth +
                    '}';
        }

        public RDFNode getNode() {
            return node;
        }
    }
}
