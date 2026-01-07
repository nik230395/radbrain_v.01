package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when quiz cannot be published
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class QuizNotPublishableException extends BusinessRuleException {
    public QuizNotPublishableException(String reason) {
        super("Quiz kann nicht veröffentlicht werden: " + reason);
    }
}
