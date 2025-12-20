package org.nikolic.programm.exceptions;

/**
 * Exception thrown when a requested quiz cannot be found.
 */
public class QuizNotFoundException extends RuntimeException {
    public QuizNotFoundException(String message) {
        super(message);
    }

    public QuizNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
