package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Specific exception for Question not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class QuestionNotFoundException extends ResourceNotFoundException {
    public QuestionNotFoundException(Long id) {
        super("Frage", id);
    }
}
