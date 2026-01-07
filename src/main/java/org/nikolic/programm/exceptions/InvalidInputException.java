package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when input data is invalid
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInputException extends ApplicationException {
    public InvalidInputException(String message) {
        super(message, "INVALID_INPUT", HttpStatus.BAD_REQUEST);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause, "INVALID_INPUT", HttpStatus.BAD_REQUEST);
    }
}
