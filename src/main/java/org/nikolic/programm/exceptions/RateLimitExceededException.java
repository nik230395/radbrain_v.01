package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when rate limit is exceeded
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends ApplicationException {
    public RateLimitExceededException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS);
    }

    public RateLimitExceededException() {
        super(
                "Zu viele Anfragen. Bitte versuchen Sie es später erneut.",
                "RATE_LIMIT_EXCEEDED",
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
