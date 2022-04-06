package cz.zcu.jsmahy.datamining.data;

import cz.zcu.jsmahy.datamining.util.Pair;

public final class Restriction extends Pair<String, String> {
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
