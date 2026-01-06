package org.egov.persistence.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role {
    private String code;
    private String name;
    private String tenantId;
}
