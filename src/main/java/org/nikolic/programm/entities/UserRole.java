package org.nikolic.programm.entities;

/**
 * User Role ENUM - passt zu DB: enum('user','admin')
 *
 * WICHTIG:
 * - DB speichert lowercase ('user', 'admin')
 * - Spring Security erwartet "ROLE_" Prefix
 * - @Enumerated(EnumType.STRING) mapped zu DB-Werten
 */
public enum UserRole {
    USER,   // entspricht 'user' in DB (wird automatisch lowercase gemapped)
    ADMIN;  // entspricht 'admin' in DB

    /**
     * Gibt Role mit Spring Security Prefix zurück
     * USER -> "ROLE_USER"
     * ADMIN -> "ROLE_ADMIN"
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    /**
     * Parse von String (case-insensitive)
     * Akzeptiert: "user", "USER", "admin", "ADMIN", "ROLE_USER", "ROLE_ADMIN"
     */
    public static UserRole fromString(String role) {
        if (role == null) return USER;

        // Entferne "ROLE_" Prefix falls vorhanden
        String normalized = role.toUpperCase().replace("ROLE_", "");

        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return USER; // Default fallback
        }
    }
}