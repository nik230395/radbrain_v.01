package org.nikolic.programm.exceptions;

/**
 * Exception thrown when a user is authenticated but lacks the required permissions.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
