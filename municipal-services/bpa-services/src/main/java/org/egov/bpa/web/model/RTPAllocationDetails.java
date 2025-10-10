package org.egov.bpa.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 *
 * @author Keshav Kumar
 *
 * this class hold the details of registered technical person RTP allocation details
 *
 *
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RTPAllocationDetails {

    private String id;
    private RTPCategory rtpCategory;
    private String rtpUUID;
    private String rtpName;
    private String applicationId;
    private Long assignmentDate;
    private String assignmentStatus;
    private Long changedDate;
    private String remarks;
    private AuditDetails auditDetails;
    /** JSON object to capture custom fields. */
    private Object additionalDetails;


    public enum Status {
        ASSIGNED, CHANGED
    }

    @Getter
    public enum RTPCategory {
        ENGINEER("ENGINEER"),
        ARCHITECT("ARCHITECT"),
        GENERAL_AGENCY("GENERAL_AGENCY");

        private final String value;
        RTPCategory(String value) {
            this.value = value;
        }

    }
}
