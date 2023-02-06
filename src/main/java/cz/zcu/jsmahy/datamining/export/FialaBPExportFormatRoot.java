package cz.zcu.jsmahy.datamining.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@RequiredArgsConstructor
class FialaBPExportFormatRoot {
    private final List<FialaBPExportNodeFormat> nodes;
    private final List<FialaBPExportEdgeFormat> edges;
}
