package org.egov.bpa.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanningPermitAuthorityEnum {
    DEVELOPMENT_AUTHORITY("DEVELOPMENT_AUTHORITY"),
    TACP("TOWN_AND_COUNTRY_PLANNING"),
    GMDA("GUWAHATI_METROPOLITAN_DEVELOPMENT_AUTHORITY");

    private final String value;
}
