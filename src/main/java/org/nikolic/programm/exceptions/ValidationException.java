package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when validation fails
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends ApplicationException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String field, String message) {
        super(
                String.format("Validierung fehlgeschlagen für '%s': %s", field, message),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST
        );
    }
}
