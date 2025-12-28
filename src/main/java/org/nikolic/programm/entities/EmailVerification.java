package org.nikolic.programm.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * EmailVerification Entity
 *
 * Ersetzt die @Transient Felder in User
 * Speichert Email-Verifizierungsdaten persistent
 */
@Entity
@Table(name = "email_verifications", indexes = {
        @Index(name = "idx_verification_user", columnList = "user_id"),
        @Index(name = "idx_verification_expires", columnList = "expires_at")
})
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "verification_code", nullable = false, length = 10)
    private String verificationCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    // Constructors
    public EmailVerification() {
        this.createdAt = LocalDateTime.now();
    }

    public EmailVerification(User user, String verificationCode, int validMinutes) {
        this();
        this.user = user;
        this.verificationCode = verificationCode;
        this.expiresAt = LocalDateTime.now().plusMinutes(validMinutes);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    // Business Logic
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !verified && !isExpired();
    }

    public void markAsVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public boolean hasExceededMaxAttempts() {
        return attempts >= 5; // Max 5 Versuche
    }

    public void regenerateCode(String newCode, int validMinutes) {
        this.verificationCode = newCode;
        this.expiresAt = LocalDateTime.now().plusMinutes(validMinutes);
        this.attempts = 0;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "EmailVerification{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", verified=" + verified +
                ", expired=" + isExpired() +
                ", attempts=" + attempts +
                '}';
    }
}