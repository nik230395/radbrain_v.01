package org.nikolic.programm.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Name ist erforderlich")
    @Size(min = 2, max = 100, message = "Name muss zwischen 2 und 100 Zeichen lang sein")
    private String fullname;

    @NotBlank(message = "E-Mail-Adresse ist erforderlich")
    @Email(message = "Ungültige E-Mail-Adresse")
    @Size(max = 255, message = "E-Mail-Adresse zu lang")
    private String email;

    @NotBlank(message = "Passwort ist erforderlich")
    @Size(min = 8, max = 100, message = "Passwort muss zwischen 8 und 100 Zeichen lang sein")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Passwort muss Groß- und Kleinbuchstaben sowie Zahlen enthalten"
    )
    private String password;

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}