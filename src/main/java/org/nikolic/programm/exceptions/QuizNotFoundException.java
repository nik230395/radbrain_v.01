package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Specific exception for Quiz not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class QuizNotFoundException extends ResourceNotFoundException {
    public QuizNotFoundException(Long id) {
        super("Quiz", id);
    }

    public QuizNotFoundException(String message) {
        super(message);
    }
}
