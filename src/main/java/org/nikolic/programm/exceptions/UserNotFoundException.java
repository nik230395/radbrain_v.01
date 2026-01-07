package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Specific exception for User not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long id) {
        super("Benutzer", id);
    }

    public UserNotFoundException(String email) {
        super("Benutzer", "E-Mail", email);
    }
}
