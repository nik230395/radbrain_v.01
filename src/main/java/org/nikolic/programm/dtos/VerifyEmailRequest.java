package org.nikolic.programm.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyEmailRequest {

    @NotBlank(message = "E-Mail-Adresse ist erforderlich")
    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;

    @NotBlank(message = "Verifizierungscode ist erforderlich")
    @Pattern(regexp = "^[0-9]{6}$", message = "Code muss 6 Ziffern haben")
    private String code;

    // Constructors
    public VerifyEmailRequest() {}

    public VerifyEmailRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}