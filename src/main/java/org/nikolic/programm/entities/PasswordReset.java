package org.nikolic.programm.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PasswordReset Entity
 *
 * Ersetzt die @Transient Password-Reset-Felder in User
 * Speichert Password-Reset-Daten persistent
 */
@Entity
@Table(name = "password_resets", indexes = {
        @Index(name = "idx_password_reset_user", columnList = "user_id"),
        @Index(name = "idx_password_reset_token", columnList = "reset_token"),
        @Index(name = "idx_password_reset_expires", columnList = "expires_at")
})
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reset_code", nullable = false, length = 10)
    private String resetCode;

    @Column(name = "reset_token", length = 64)
    private String resetToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // Constructors
    public PasswordReset() {
        this.createdAt = LocalDateTime.now();
    }

    public PasswordReset(User user, String resetCode, int validMinutes) {
        this();
        this.user = user;
        this.resetCode = resetCode;
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

    public String getResetCode() {
        return resetCode;
    }

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    // Business Logic Methods

    /**
     * Prüft ob Reset-Code abgelaufen ist
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Prüft ob Reset-Token abgelaufen ist
     */
    public boolean isTokenExpired() {
        return tokenExpiresAt != null && LocalDateTime.now().isAfter(tokenExpiresAt);
    }

    /**
     * Prüft ob Reset noch gültig ist
     */
    public boolean isValid() {
        return !used && !isExpired();
    }

    /**
     * Prüft ob Token noch gültig ist
     */
    public boolean isTokenValid() {
        return !used && resetToken != null && !isTokenExpired();
    }

    /**
     * Markiert Reset als verwendet
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Erhöht Versuchszähler
     */
    public void incrementAttempts() {
        this.attempts++;
    }

    /**
     * Prüft ob maximale Versuche überschritten
     */
    public boolean hasExceededMaxAttempts() {
        return attempts >= 5; // Max 5 Versuche
    }

    /**
     * Generiert Reset-Token nach Code-Verifikation
     */
    public void generateToken(String token, int validMinutes) {
        this.resetToken = token;
        this.tokenExpiresAt = LocalDateTime.now().plusMinutes(validMinutes);
    }

    /**
     * Erneuert Reset-Code
     */
    public void regenerateCode(String newCode, int validMinutes) {
        this.resetCode = newCode;
        this.expiresAt = LocalDateTime.now().plusMinutes(validMinutes);
        this.attempts = 0;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PasswordReset{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", used=" + used +
                ", expired=" + isExpired() +
                ", attempts=" + attempts +
                ", createdAt=" + createdAt +
                '}';
    }
}