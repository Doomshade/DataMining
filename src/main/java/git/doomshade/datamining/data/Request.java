package git.doomshade.datamining.data;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class Request {
    private final String requestPage, namespace, link;
    private final Collection<Restriction> restrictions = new LinkedList<>();

    /**
     * @param requestPage the request to send to the web page, e.g. {@code Windows_10}
     * @param namespace   the namespace, e.g. <a href="https://dbpedia.org/property/">a property</a> or <a
     *                    href="https://dbpedia.org/ontology/">an ontology</a>
     * @param link        the link to create the ontology for, e.g. {@code precededBy}
     */
    public Request(String requestPage, String namespace, String link) {
        this.requestPage = requestPage;
        this.namespace = namespace;
        this.link = link;
    }

    public void addRestriction(Restriction restriction) {
        restrictions.add(restriction);
    }

    public void addRestrictions(Collection<? extends Restriction> restrictions) {
        this.restrictions.addAll(restrictions);
    }

    public Collection<Restriction> getRestrictions() {
        return Collections.unmodifiableCollection(restrictions);
    }

    public String getRequestPage() {
        return requestPage;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getLink() {
        return link;
    }
}
