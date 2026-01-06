package org.nikolic.programm.dtos;

import java.time.LocalDateTime;

public class RegistrationCacheEntry {
    private String email;
    private String fullname;
    private String passwordHash; // Never store raw password
    private String codeHash;     // SHA256 of verification code
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    // Constructors
    public RegistrationCacheEntry() {}

    public RegistrationCacheEntry(String email, String fullname, String passwordHash,
                                  String codeHash, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.email = email;
        this.fullname = fullname;
        this.passwordHash = passwordHash;
        this.codeHash = codeHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // Helper method
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}