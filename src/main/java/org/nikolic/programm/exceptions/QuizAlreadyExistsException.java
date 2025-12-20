package org.nikolic.programm.exceptions;

/**
 * Exception thrown when attempting to create a quiz that already exists.
 */
public class QuizAlreadyExistsException extends RuntimeException {
    public QuizAlreadyExistsException(String message) {
        super(message);
    }

    public QuizAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
