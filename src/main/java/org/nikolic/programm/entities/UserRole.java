package org.nikolic.programm.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * UserRole Enum - Bereinigt ohne Duplikate
 *
 * Änderungen:
 * - Duplikate entfernt (user/admin lowercase)
 * - Robuste fromString() Methode
 * - Bessere Authority-Handling
 */
public enum UserRole {
    USER("Benutzer"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Konvertiert UserRole zu Spring Security GrantedAuthority
     */
    public GrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }

    /**
     * Erstellt Authority-Liste für diese Rolle
     */
    public List<GrantedAuthority> getAuthorities() {
        return List.of(toAuthority());
    }

    /**
     * Prüft ob diese Rolle Admin-Rechte hat
     */
    public boolean hasAdminRights() {
        return this == ADMIN;
    }

    /**
     * Konvertiert String zu UserRole (case-insensitive, robust)
     *
     * Akzeptiert:
     * - "USER", "user", "User"
     * - "ADMIN", "admin", "Admin"
     * - "ROLE_USER", "ROLE_ADMIN"
     */
    public static UserRole fromString(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return USER; // Default
        }

        // Normalisierung: Trim, Uppercase, remove "ROLE_" prefix
        String normalized = roleString.trim().toUpperCase().replace("ROLE_", "");

        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Fallback bei ungültigen Werten
            return USER;
        }
    }

    /**
     * Erstellt Authority-String für Spring Security
     */
    public String getAuthorityString() {
        return "ROLE_" + this.name();
    }

    /**
     * Prüft ob ein String dieser Rolle entspricht
     */
    public boolean matches(String roleString) {
        if (roleString == null) return false;

        String normalized = roleString.trim().toUpperCase().replace("ROLE_", "");
        return this.name().equals(normalized);
    }

    @Override
    public String toString() {
        return this.name(); // Immer uppercase: "USER" oder "ADMIN"
    }
}