package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class BusinessRuleException extends ApplicationException {
    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", HttpStatus.CONFLICT);
    }
}