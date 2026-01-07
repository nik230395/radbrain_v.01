package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when token is invalid
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends ApplicationException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN", HttpStatus.UNAUTHORIZED);
    }

    public InvalidTokenException() {
        super("Token ist ungültig oder abgelaufen", "INVALID_TOKEN", HttpStatus.UNAUTHORIZED);
    }
}
