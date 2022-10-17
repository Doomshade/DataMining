package cz.zcu.jsmahy.datamining.query;

import lombok.Data;

import java.util.Collection;
import java.util.LinkedList;


@Data
public final class SparqlRequest {
	private final String requestPage, namespace, link;
	private final Collection<Restriction> restrictions = new LinkedList<>();

	/**
	 * @param requestPage the request to send to the web page, e.g. {@code Windows_10}
	 * @param namespace   the namespace, e.g. <a href="https://dbpedia.org/property/">a property</a> or <a
	 *                    href="https://dbpedia.org/ontology/">an ontology</a>
	 * @param link        the link to create the ontology for, e.g. {@code precededBy}
	 */
	public SparqlRequest(String requestPage, String namespace, String link) {
		this.requestPage = requestPage;
		this.namespace = namespace;
		this.link = link;
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
