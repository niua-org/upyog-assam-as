package org.egov.bpa.web.model.landInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandSearchCriteria {

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId;

    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("landUId")
    private String landUId;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;

    @JsonIgnore
    private List<String> userIds;
    
    @JsonProperty("locality")
    private String locality;

    @JsonProperty("name")
    private String name;

    @JsonProperty("isInboxSearch")
    private Boolean isInboxSearch;

    public boolean isEmpty() {
        return (this.tenantId == null && this.ids == null && this.landUId == null && this.mobileNumber == null && this.name==null && locality == null);
    }

    public boolean tenantIdOnly() {
        return (this.tenantId != null && this.ids == null && this.landUId == null && this.mobileNumber == null && this.name==null && locality == null);
    }
}
