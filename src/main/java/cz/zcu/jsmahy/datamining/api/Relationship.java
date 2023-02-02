package cz.zcu.jsmahy.datamining.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class Relationship extends DefaultArbitraryDataHolder {
    private final long from;
    private final long to;
}
