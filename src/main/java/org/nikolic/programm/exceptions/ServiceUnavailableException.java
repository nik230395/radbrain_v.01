package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when external service is unavailable
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends ApplicationException {
    public ServiceUnavailableException(String serviceName) {
        super(
                String.format("Service '%s' ist vorübergehend nicht verfügbar", serviceName),
                "SERVICE_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super(
                String.format("Service '%s' ist vorübergehend nicht verfügbar", serviceName),
                cause,
                "SERVICE_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
