package cz.zcu.jsmahy.datamining.data.infobox;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class InfoboxTemplate implements Serializable {
    private final String name;
    private final Collection<String> requiredAttributes = new HashSet<>();
    private final Collection<String> optionalAttributes = new HashSet<>();

    public InfoboxTemplate(String name, Collection<String> requiredAttributes, Collection<String> optionalAttributes) {
        this.name = name;
        this.requiredAttributes.addAll(requiredAttributes);
        this.optionalAttributes.addAll(optionalAttributes);
    }

    public final String getName() {
        return name;
    }

    public final Collection<String> getRequiredAttributes() {
        return Collections.unmodifiableCollection(requiredAttributes);
    }

    public final Collection<String> getOptionalAttributes() {
        return Collections.unmodifiableCollection(optionalAttributes);
    }
}
