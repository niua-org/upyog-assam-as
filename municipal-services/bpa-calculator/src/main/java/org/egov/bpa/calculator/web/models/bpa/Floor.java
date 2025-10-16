package org.egov.bpa.calculator.web.models.bpa;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Floor {

	private Integer level;
	private BigDecimal builtUpArea;
	private BigDecimal floorArea;

}
