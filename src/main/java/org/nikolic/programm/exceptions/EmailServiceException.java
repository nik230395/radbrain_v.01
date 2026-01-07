package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when email service fails
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class EmailServiceException extends ServiceUnavailableException {
    public EmailServiceException(Throwable cause) {
        super("E-Mail", cause);
    }

    public EmailServiceException(String message) {
        super("E-Mail");
    }
}
