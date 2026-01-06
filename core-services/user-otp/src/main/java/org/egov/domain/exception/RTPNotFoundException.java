package org.egov.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RTPNotFoundException extends RuntimeException {
	private static final long serialVersionUID = -604414276256051721L;

	private String code;
	private String message;
}
