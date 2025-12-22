package org.nikolic.programm.dtos;

public class PasswordResetRequest {

    private String email;
    private String code;
    private String newPassword;

    // Constructors
    public PasswordResetRequest() {}

    public PasswordResetRequest(String email, String code, String newPassword) {
        this.email = email;
        this.code = code;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}