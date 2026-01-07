package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when credentials are invalid
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends ApplicationException {
    public InvalidCredentialsException() {
        super(
                "Ungültige Anmeldedaten",
                "INVALID_CREDENTIALS",
                HttpStatus.UNAUTHORIZED
        );
    }

    public InvalidCredentialsException(String message) {
        super(
                message,
                "INVALID_CREDENTIALS",
                HttpStatus.UNAUTHORIZED
        );
    }
}
