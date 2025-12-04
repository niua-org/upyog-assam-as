package org.egov.noc.web.model.aai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for AAI NOCAS API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AAIStatusResponse {

    private List<AAIApplicationStatus> applicationStatuses;

    private String responseStatus;

    private String errorMessage;

    private Boolean success;
}

