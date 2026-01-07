package org.nikolic.programm.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasswordResetRequest {

    @NotBlank(message = "E-Mail-Adresse ist erforderlich")
    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;

    @NotBlank(message = "Verifizierungscode ist erforderlich")
    @Pattern(regexp = "^[0-9]{6}$", message = "Code muss 6 Ziffern haben")
    private String code;

    @NotBlank(message = "Neues Passwort ist erforderlich")
    @Size(min = 8, max = 100, message = "Passwort muss zwischen 8 und 100 Zeichen lang sein")
    private String newPassword;

    // Constructors
    public PasswordResetRequest() {}

    public PasswordResetRequest(String email, String code, String newPassword) {
        this.email = email;
        this.code = code;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    @Override
    public String toString() {
        return "PasswordResetRequest{" +
                "email='" + email + '\'' +
                ", code='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                '}';
    }
}