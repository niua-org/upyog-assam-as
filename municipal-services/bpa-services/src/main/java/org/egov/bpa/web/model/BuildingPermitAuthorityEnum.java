package org.egov.bpa.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BuildingPermitAuthorityEnum {

    MUNICIPAL_BOARD("MUNICIPAL_BOARD"),
    GRAM_PANCHAYAT("GRAM_PANCHAYAT"),
    GMC("GUWAHATI_MUNICIPAL_CORPORATION"),
    NGMB("NORTH_GUWAHATI_MUNICIPAL_BOARD"),

    UNKNOWN("UNKNOWN");  

    private final String value;

    @JsonCreator
    public static BuildingPermitAuthorityEnum fromValue(String input) {
        if (input == null) {
            return UNKNOWN;
        }

        for (BuildingPermitAuthorityEnum e : BuildingPermitAuthorityEnum.values()) {
           
            if (e.value.equalsIgnoreCase(input) || e.name().equalsIgnoreCase(input)) {
                return e;
            }
        }

        return UNKNOWN;
    }
}
