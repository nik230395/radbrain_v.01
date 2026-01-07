package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationFailedException extends ApplicationException {
    public AuthenticationFailedException(String message) {
        super(message, "AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED);
    }
}