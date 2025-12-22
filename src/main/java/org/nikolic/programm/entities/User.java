package org.nikolic.programm.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java. util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String fullname;

    private String password_hash;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean is_active;

    private LocalDateTime created_at;

    // ✅ TEMPORÄR: Email-Verification-Felder als Transient (nicht in DB)
    @Transient
    private boolean emailVerified = true; // Default true für bestehende Users

    @Transient
    private String verificationCode;

    @Transient
    private LocalDateTime verificationCodeExpiry;

    @Transient
    private String passwordResetCode;

    @Transient
    private LocalDateTime passwordResetExpiry;

    @Transient
    private String passwordResetToken;

    @Transient
    private LocalDateTime passwordResetTokenExpiry;

    // Constructors
    public User() {}

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getPassword_hash() { return password_hash; }
    public void setPassword_hash(String password_hash) { this.password_hash = password_hash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return is_active; }
    public boolean getIs_active() { return is_active; }
    public void setIs_active(boolean is_active) { this.is_active = is_active; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }

    // Email Verification (Transient)
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public LocalDateTime getVerificationCodeExpiry() { return verificationCodeExpiry; }
    public void setVerificationCodeExpiry(LocalDateTime verificationCodeExpiry) {
        this.verificationCodeExpiry = verificationCodeExpiry;
    }

    // Password Reset (Transient)
    public String getPasswordResetCode() { return passwordResetCode; }
    public void setPasswordResetCode(String passwordResetCode) { this.passwordResetCode = passwordResetCode; }

    public LocalDateTime getPasswordResetExpiry() { return passwordResetExpiry; }
    public void setPasswordResetExpiry(LocalDateTime passwordResetExpiry) {
        this.passwordResetExpiry = passwordResetExpiry;
    }

    public String getPasswordResetToken() { return passwordResetToken; }
    public void setPasswordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; }

    public LocalDateTime getPasswordResetTokenExpiry() { return passwordResetTokenExpiry; }
    public void setPasswordResetTokenExpiry(LocalDateTime passwordResetTokenExpiry) {
        this.passwordResetTokenExpiry = passwordResetTokenExpiry;
    }

    // Admin-Related Methods
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isUser() {
        return this.role == UserRole.USER;
    }

    public String getRoleString() {
        return this.role != null ? this.role.toString() : "USER";
    }

    public boolean hasRole(UserRole role) {
        return this.role == role;
    }

    public boolean hasRole(String roleName) {
        if (this.role == null || roleName == null) {
            return false;
        }
        return this.role.toString().equalsIgnoreCase(roleName) ||
                this.role. toString().equalsIgnoreCase("ROLE_" + roleName);
    }

    public List<String> getRoles() {
        return this.role != null ? List.of(this.role.toString()) : List.of();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullname='" + fullname + '\'' +
                ", role=" + role +
                ", is_active=" + is_active +
                ", emailVerified=" + emailVerified +
                ", created_at=" + created_at +
                '}';
    }
}