package cz.zcu.jsmahy.datamining.data;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public final class SparqlRequest implements ISparqlRequest {
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
		this.namespace   = namespace;
		this.link        = link;
	}

	@Override
	public void addRestriction(Restriction restriction) {
		this.restrictions.add(restriction);
	}

	@Override
	public void addRestrictions(Iterable<? extends Restriction> restrictions) {
		restrictions.forEach(this::addRestriction);
	}

	@Override
	public Iterable<Restriction> getRestrictions() {
		return Collections.unmodifiableCollection(restrictions);
	}

	@Override
	public String getRequestPage() {
		return requestPage;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getLink() {
		return link;
	}
}
