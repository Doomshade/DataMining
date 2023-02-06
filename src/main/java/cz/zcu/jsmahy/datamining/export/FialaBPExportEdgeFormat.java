package cz.zcu.jsmahy.datamining.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
class FialaBPExportEdgeFormat {
    private long id = -1;
    private String stereotype = null;
    private long from = -1;
    private long to = -1;
    private String name = null;
}
