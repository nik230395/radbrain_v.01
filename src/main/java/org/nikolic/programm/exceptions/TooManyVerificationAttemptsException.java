package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when verification attempts are exceeded
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyVerificationAttemptsException extends RateLimitExceededException {
    public TooManyVerificationAttemptsException() {
        super("Zu viele Verifizierungsversuche. Bitte fordern Sie einen neuen Code an.");
    }
}
