package org.nikolic.programm.entities;

import org.springframework.security.core. GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public enum UserRole {
    USER("Benutzer"),
    ADMIN("Administrator"),
    user("Benutzer"),      // ✅ Lowercase für Kompatibilität
    admin("Administrator"); // ✅ Lowercase für Kompatibilität

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Konvertiert UserRole zu GrantedAuthority
     */
    public GrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + this.name().toUpperCase());
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
        return this == ADMIN || this == admin;
    }

    /**
     * Konvertiert String zu UserRole (case-insensitive)
     */
    public static UserRole fromString(String roleString) {
        if (roleString == null) return USER;

        String cleanRole = roleString.trim();

        // Versuche exakte Matches
        for (UserRole role : UserRole. values()) {
            if (role.name().equalsIgnoreCase(cleanRole)) {
                return role;
            }
        }

        // Fallback
        if ("admin".equalsIgnoreCase(cleanRole)) return ADMIN;
        if ("user".equalsIgnoreCase(cleanRole)) return USER;

        return USER; // Default fallback
    }

    /**
     * Erstellt Authority-String
     */
    public String getAuthorityString() {
        return "ROLE_" + this.name().toUpperCase();
    }

    @Override
    public String toString() {
        return this.name().toUpperCase(); // Immer uppercase für Konsistenz
    }
}