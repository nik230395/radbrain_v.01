package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when email is not verified
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class EmailNotVerifiedException extends ApplicationException {
    public EmailNotVerifiedException() {
        super(
                "Bitte verifizieren Sie Ihre E-Mail-Adresse",
                "EMAIL_NOT_VERIFIED",
                HttpStatus.FORBIDDEN
        );
    }
}
