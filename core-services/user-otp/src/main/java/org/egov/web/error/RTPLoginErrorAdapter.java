package org.egov.web.error;

import java.util.ArrayList;
import java.util.List;

import org.egov.domain.exception.RTPNotFoundException;
import org.egov.web.contract.Error;
import org.egov.web.contract.ErrorField;
import org.egov.web.contract.ErrorResponse;
import org.springframework.http.HttpStatus;

public class RTPLoginErrorAdapter  implements ErrorAdapter<RTPNotFoundException>  {

    private static final String ERROR_FIELD = "otp.mobileNumber";

    @Override
    public ErrorResponse adapt(RTPNotFoundException ex) {

        List<ErrorField> errorFields = new ArrayList<>();

        ErrorField errorField = ErrorField.builder()
                .code(ex.getCode())          // ← your code
                .message(ex.getMessage())    // ← your message
                .field(ERROR_FIELD)
                .build();

        errorFields.add(errorField);

        Error error = Error.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())    // ← your message only
                .fields(errorFields)
                .build();

        return new ErrorResponse(null, error);
    }
}

