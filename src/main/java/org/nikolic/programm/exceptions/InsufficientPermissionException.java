package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when user lacks permission
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class InsufficientPermissionException extends ApplicationException {
    public InsufficientPermissionException(String message) {
        super(message, "INSUFFICIENT_PERMISSION", HttpStatus.FORBIDDEN);
    }

    public InsufficientPermissionException() {
        super(
                "Sie haben keine Berechtigung für diese Aktion",
                "INSUFFICIENT_PERMISSION",
                HttpStatus.FORBIDDEN
        );
    }
}
