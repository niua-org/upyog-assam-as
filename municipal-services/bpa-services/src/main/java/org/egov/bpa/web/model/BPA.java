package org.egov.bpa.web.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.bpa.web.model.NOC.NocType;
import org.egov.bpa.web.model.landInfo.LandInfo;
import org.egov.bpa.web.model.property.PropertyValidationResponse;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BPA application object to capture the details of land, land owners, and address of the land.
 */
@ApiModel(description = "BPA application object to capture the details of land, land owners, and address of the land.")
@Validated
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BPA {

  /** Unique Identifier(UUID) of the BPA application for internal reference. */
  @SafeHtml
  @Size(min = 1, max = 64)
  private String id;

  /** Formatted unique identifier of the building permit application. */
  @SafeHtml
  @Size(min = 1, max = 64)
  private String applicationNo;

  /** Unique ULB identifier. */
  @SafeHtml
  @NotNull
  @Size(min = 2, max = 256)
  private String tenantId;

  /** Unique identifier of the scrutinized EDCR number. */
  @SafeHtml
  @Size(min = 1, max = 64)
  private String edcrNumber;

  /** Status of the application. */
  @SafeHtml
  private String status;

  /** Application submission date. */
  private Long applicationDate;
  
  private String planningPermitNo;
  private Long planningPermitDate;
  private String ppFileStoreId;

  private String buildingPermitNo;
  private Long buildingPermitDate;
  private String bpFileStoreId;

  private String occupancyCertificateNo;
  private Long occupancyCertificateDate;
  private String ocFileStoreId;

  private String ppFeeReceiptFileStoreId;
  private String bpFeeReceiptFileStoreId;

  private String propertyNo;
  /**
   * Stores the complete validation response received from Property service
   * along with property details like owner name, address, phone, etc.
   */
  private PropertyValidationResponse propertyDetails;
  private String propertyVendor;

  /** Approval number based on workflow status. */
  @SafeHtml
  @Size(min = 1, max = 64)
  private String approvalNo;

  /** Approval date based on workflow status. */
  private Long approvalDate;

  /** Business service associated with the application. */
  @SafeHtml
  @Size(min = 1, max = 64)
  private String businessService;

  /** Initiator user UUID. */
  @SafeHtml
  @Size(min = 1, max = 64)
  private String accountId;

  /** Type of application. */
  @SafeHtml
  private String applicationType;

  /** Risk type derived from MDMS configuration. */
  @SafeHtml
  @Size(min = 1, max = 64)
  private String riskType;

  /** Unique Identifier(UUID) of the land for internal reference. */
  @SafeHtml
  @Size(min = 1, max = 64)
  private String landId;

  @JsonProperty("createdBy")
  private String createdBy = null;

  @JsonProperty("lastModifiedBy")
  private String lastModifiedBy = null;

  @JsonProperty("createdTime")
  private Long createdTime = null;

  @JsonProperty("lastModifiedTime")
  private Long lastModifiedTime = null;

  /** List of documents attached by the owner for exemption. */
  @Valid
  private List<Document> documents = new ArrayList<>();

  /** Land information associated with the application. */
  private LandInfo landInfo;

  /** Workflow details of the application. */
  private Workflow workflow;

  /** Audit details of the application. */
  private AuditDetails auditDetails;

  private RTPAllocationDetails rtpDetails;

  private AreaMappingDetail areaMapping;

  /** JSON object to capture custom fields. */
  private Object additionalDetails;
  
  private List<NocType> nocList;

  public void addDocument(Document documentsItem) {
    if (this.documents == null) {
      this.documents = new ArrayList<>();
    }
    this.documents.add(documentsItem);
  }
}