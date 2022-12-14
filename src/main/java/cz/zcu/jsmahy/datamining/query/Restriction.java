package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.util.Pair;

public final class Restriction extends Pair<String, String> {
    public Restriction(final String key, final String value) {
        super(key, value);
    }

    /**
     * @return The namespace.
     */
    @Override
    public String getKey() {
        return super.getKey();
    }

    /**
     * @return The link.
     */
    @Override
    public String getValue() {
        return super.getValue();
    }
}
