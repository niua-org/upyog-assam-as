package org.egov.bpa.web.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contract class to receive request. Array of Property items are used in case
 * of create. Whereas a single Property item is used for update.
 */
@ApiModel(description = "Contract class to receive request. Array of Property items are used in case of create. Whereas a single Property item is used for update.")
@Validated
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BPARequest {

	/** Request information for the BPA request */
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	/** BPA object containing the details of the request */
	@JsonProperty("BPA")
	private BPA BPA;

}