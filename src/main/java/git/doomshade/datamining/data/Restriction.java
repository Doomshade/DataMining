package git.doomshade.datamining.data;

import git.doomshade.datamining.util.Pair;

public class Restriction extends Pair<String, String> {
    public Restriction(final String namespace, final String link) {
        super(namespace, link);
    }

    public String getNamespace() {
        return super.key;
    }

    public String getLink() {
        return super.value;
    }
}
