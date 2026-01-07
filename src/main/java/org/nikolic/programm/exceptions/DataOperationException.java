package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DataOperationException extends ApplicationException {
    public DataOperationException(String message) {
        super(message, "DATA_OPERATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DataOperationException(String message, Throwable cause) {
        super(message, cause, "DATA_OPERATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
