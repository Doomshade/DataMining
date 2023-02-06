package cz.zcu.jsmahy.datamining.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
class FialaBPExportNodeFormat {
    private long id = -1;
    private String stereotype = null;
    private String name = null;
    private String description = null;
    private Calendar begin = null;
    private Calendar end = null;
    private Map<String, String> properties = null;
    private List<SubItem> subItems = null;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class SubItem {
        private long id = -1;
        private String name = null;
        private String type = null;
        private Date begin = null;
        private Date end = null;
        private String css = null;
    }
}
