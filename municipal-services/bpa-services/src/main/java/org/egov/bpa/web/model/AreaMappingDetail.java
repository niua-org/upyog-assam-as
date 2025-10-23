package org.egov.bpa.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AreaMappingDetail {
    private String id;
    private String applicationId;
    private String district;
    private String planningArea;
    private PlanningPermitAuthorityEnum planningPermitAuthority;
    private BuildingPermitAuthorityEnum buildingPermitAuthority;
    private String revenueVillage;
    private String mouza;
    private String ward;
    private String village;
}
