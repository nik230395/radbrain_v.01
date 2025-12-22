package org.nikolic.programm.dtos;

public class RegisterRequest {
    private String email;
    private String password;
    private String fullname;

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
}