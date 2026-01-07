package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when resource already exists
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends ApplicationException {
    public ResourceAlreadyExistsException(String resourceName, String field, String value) {
        super(
                String.format("%s mit %s '%s' existiert bereits", resourceName, field, value),
                "RESOURCE_EXISTS",
                HttpStatus.CONFLICT
        );
    }
}
