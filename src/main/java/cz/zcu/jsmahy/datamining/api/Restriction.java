package cz.zcu.jsmahy.datamining.api;

import lombok.Data;

/**
 * Works as a filter to determine whether the target node should be accounted for when building the line. The target node has this node as a requirement. For example, a requirement could be that the
 * target node has to have a name.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
@Data
public final class Restriction {
    private final String namespace;
    private final String link;
}
