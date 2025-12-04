package org.egov.noc.web.model.aai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for NOC application status from AAI NOCAS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AAIApplicationStatus {

    private String applicationNumber;

    private String status;

    private String remarks;

    private String nocCertificateNumber;

    private Long issueDate;

    private Long validityDate;

    private byte[] documentData;

    private String documentFileName;
}

