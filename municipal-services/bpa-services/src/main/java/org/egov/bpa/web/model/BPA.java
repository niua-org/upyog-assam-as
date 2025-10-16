package org.egov.bpa.web.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.bpa.web.model.landInfo.LandInfo;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;

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

	private BigDecimal totalBuiltUpArea;
	private List<Floor> floors;
	private String wallType;
	private String constructionType;
	private String feeType;

  public void addDocument(Document documentsItem) {
    if (this.documents == null) {
      this.documents = new ArrayList<>();
    }
    this.documents.add(documentsItem);
  }
}