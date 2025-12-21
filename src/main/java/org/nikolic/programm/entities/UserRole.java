package org.nikolic.programm.entities;

/**
 * Enum for user roles in the system.
 * Replaces the legacy Role entity.
 */
public enum UserRole {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    /**
     * Parse a role name string to UserRole enum.
     * Supports both "USER"/"ADMIN" and "ROLE_USER"/"ROLE_ADMIN" formats.
     */
    public static UserRole fromString(String roleName) {
        if (roleName == null) {
            return USER; // default to USER
        }
        
        String normalized = roleName.toUpperCase();
        if (normalized.equals("ADMIN") || normalized.equals("ROLE_ADMIN")) {
            return ADMIN;
        }
        
        return USER; // default to USER
    }
}
