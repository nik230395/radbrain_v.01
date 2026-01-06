package org.nikolic.programm.entities;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Entity - Verbesserte Version
 *
 * Änderungen:
 * - Email Verification in eigene Entity ausgelagert
 * - Audit Trail hinzugefügt
 * - Konsistente Naming Convention
 * - Bessere Validierung
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_active", columnList = "is_active")
})
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String fullname;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship zu EmailVerification (One-to-One)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private EmailVerification emailVerification;

    // Relationship zu PasswordResets (One-to-Many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordReset> passwordResets;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String email, String fullname, String passwordHash) {
        this();
        this.email = email.toLowerCase().trim();
        this.fullname = fullname.trim();
        this.passwordHash = passwordHash;
    }

    // Getters and Setters mit konsistenter Naming
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname != null ? fullname.trim() : null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role != null ? role : UserRole.USER;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public EmailVerification getEmailVerification() {
        return emailVerification;
    }

    public void setEmailVerification(EmailVerification emailVerification) {
        this.emailVerification = emailVerification;
        if (emailVerification != null) {
            emailVerification.setUser(this);
        }
    }

    public List<PasswordReset> getPasswordResets() {
        return passwordResets;
    }

    public void setPasswordResets(List<PasswordReset> passwordResets) {
        this.passwordResets = passwordResets;
    }

    // Helper Methods - Vereinfacht
    public boolean isEmailVerified() {
        return emailVerification != null && emailVerification.isVerified();
    }

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
        String normalizedRole = roleName.toUpperCase().replace("ROLE_", "");
        return this.role.toString().equals(normalizedRole);
    }

    public List<String> getRoles() {
        return this.role != null ? List.of(this.role.toString()) : List.of("USER");
    }

    // Business Logic
    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = LocalDateTime.now();
    }

    public void promoteToAdmin() {
        this.role = UserRole.ADMIN;
    }

    public void demoteToUser() {
        this.role = UserRole.USER;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullname='" + fullname + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", emailVerified=" + isEmailVerified() +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}