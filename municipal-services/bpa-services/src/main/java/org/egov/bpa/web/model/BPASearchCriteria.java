package org.egov.bpa.web.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BPASearchCriteria {

    private String tenantId;

    private List<String> ids;

    private String status;

    private String edcrNumber;

    private String applicationNo;

    private String approvalNo;

    private String mobileNumber;

    @JsonIgnore
    private List<String> landId;

    private Integer offset;

    private Integer limit;

    private Long approvalDate;

    private Long fromDate;

    private Long toDate;

    @JsonIgnore
    private List<String> ownerIds;

    @JsonIgnore
    private List<String> businessService;

    @JsonIgnore
    private List<String> createdBy;

    private String locality;

    private String applicationType;

    private String serviceType;

    private String permitNumber;

    private String riskType;

    private String name;

    private String district;

    @JsonIgnore
    @Builder.Default
    private Boolean isInboxSearch = false;

    public boolean isEmpty() {
        return (this.tenantId == null && this.status == null && this.ids == null && this.applicationNo == null
                && this.mobileNumber == null && this.name == null && this.landId == null && this.edcrNumber == null && this.approvalNo == null
                && this.approvalDate == null && this.ownerIds == null && this.district == null
                && this.businessService == null && this.locality == null && this.applicationType == null && this.serviceType == null
                && this.permitNumber == null);
    }

    public boolean tenantIdOnly() {
        return (this.tenantId != null && this.status == null && this.ids == null && this.applicationNo == null
                && this.mobileNumber == null && this.name == null && this.landId == null && this.edcrNumber == null && this.approvalNo == null
                && this.approvalDate == null && this.ownerIds == null && this.district == null
                && this.businessService == null && this.locality == null && this.applicationType == null && this.serviceType == null
                && this.permitNumber == null);
    }
}