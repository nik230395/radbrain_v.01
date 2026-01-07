package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(
                String.format("%s mit ID %d nicht gefunden", resourceName, id),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String resourceName, String identifier, String value) {
        super(
                String.format("%s mit %s '%s' nicht gefunden", resourceName, identifier, value),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
