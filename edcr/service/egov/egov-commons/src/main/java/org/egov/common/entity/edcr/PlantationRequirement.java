package org.egov.common.entity.edcr;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlantationRequirement extends MdmsFeatureRule {
	
	 @JsonProperty("percent")
	    private BigDecimal percent;
	 
	 private BigDecimal noOfTreesToBePlant;


	public BigDecimal getNoOfTreesToBePlant() {
		return noOfTreesToBePlant;
	}

	public void setNoOfTreesToBePlant(BigDecimal noOfTreesToBePlant) {
		this.noOfTreesToBePlant = noOfTreesToBePlant;
	}

	public BigDecimal getPercent() {
		return percent;
	}

	public void setPercent(BigDecimal percent) {
		this.percent = percent;
	}

	@Override
	public String toString() {
		return "PlantationRequirement [percent=" + percent + ", noOfTreesToBePlant=" + noOfTreesToBePlant + "]";
	}

}
