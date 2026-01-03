package org.nikolic.programm.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ✅ FIXED LoginRequest DTO
 *
 * Changes:
 * - Added Jakarta Bean Validation annotations
 * - Added custom validation messages in German
 */
public class LoginRequest {

    @NotBlank(message = "E-Mail-Adresse ist erforderlich")
    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;

    @NotBlank(message = "Passwort ist erforderlich")
    @Size(min = 1, message = "Passwort darf nicht leer sein")
    private String password;

    // Constructors
    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}