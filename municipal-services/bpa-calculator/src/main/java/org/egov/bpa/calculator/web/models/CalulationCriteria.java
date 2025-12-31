package org.egov.bpa.calculator.web.models;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.bpa.calculator.web.models.bpa.Floor;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalulationCriteria {

	@JsonProperty("BPA")
	@Valid
	private BPA bpa = null;

	@JsonProperty("applicationNo")
	private String applicationNo = null;
	
	@JsonProperty("subOccupancy")
	@Size(min = 2, max = 64)
	private String subOccupancy;

	@JsonProperty("floorLevel")
	private String floorLevel = null;
	
	private List<Floor> floors;

	@JsonProperty("wallType")
	private String wallType = null;

	@JsonProperty("applicationType")
	private String applicationType = null;

	@JsonProperty("tenantId")
	@NotNull
	@Size(min = 2, max = 256)
	private String tenantId = null;
	
	@JsonProperty("feeType")
	@NotNull
	@Size(min = 2, max = 64)
	private String feeType = null;
	
	private BigDecimal premiumBuiltUpArea;


}
