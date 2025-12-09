package org.egov.bpa.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanningPermitAuthorityEnum {

    DEVELOPMENT_AUTHORITY("DEVELOPMENT_AUTHORITY"),
    TACP("TOWN_AND_COUNTRY_PLANNING"),
    GMDA("GUWAHATI_METROPOLITAN_DEVELOPMENT_AUTHORITY"),

    UNKNOWN("UNKNOWN");   

    private final String value;

    @JsonCreator
    public static PlanningPermitAuthorityEnum fromValue(String input) {
        if (input == null) {
            return UNKNOWN;
        }
        for (PlanningPermitAuthorityEnum e : PlanningPermitAuthorityEnum.values()) {
            if (e.value.equalsIgnoreCase(input) || e.name().equalsIgnoreCase(input)) {
                return e;
            }
        }
        return UNKNOWN; 
    }
}
