package cz.zcu.jsmahy.datamining.query;

import org.apache.jena.rdf.model.RDFNode;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.io.PrintStream;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class Ontology {
	private final DirectedAcyclicGraph<RDFNode, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
	private final RDFNode root;

	public Ontology(final RDFNode root) {
		this.root = root;
		graph.addVertex(root);
	}

	public RDFNode getRoot() {
		return root;
	}

	public Graph<RDFNode, DefaultEdge> getGraph() {
		return graph;
	}

	@Override
	public String toString() {
		return graph.toString();
	}

	public void printOntology(PrintStream out) {
		out.println(graph);
	}

	public void printOntology(StringBuilder sb) {
		//new GraphSPARQLService("");
		//sb.append(graph);
	}

	/**
	 * Adds an oriented edge to the graph
	 *
	 * @param from the first point
	 * @param to   the second point
	 */
	public synchronized void addEdge(final RDFNode from, final RDFNode to) {
		org.apache.jena.graph.Graph g;
		graph.addVertex(from);
		graph.addVertex(to);
		if (graph.addEdge(from, to) == null) {
			// TODO throw something
			System.err.printf("Failed to add edge %s-%s%n", from, to);
		}
	}
}
