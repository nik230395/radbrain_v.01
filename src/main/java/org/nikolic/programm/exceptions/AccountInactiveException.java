package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountInactiveException extends ApplicationException {
    public AccountInactiveException() {
        super(
                "Ihr Konto ist inaktiv. Bitte kontaktieren Sie den Support.",
                "ACCOUNT_INACTIVE",
                HttpStatus.FORBIDDEN
        );
    }
}