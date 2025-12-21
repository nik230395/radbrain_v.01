package org.nikolic.programm.enums;

/**
 * Enum for user roles in the system.
 * Replaces the legacy Role entity with a simple enum for better performance and maintainability.
 */
public enum UserRole {
    USER("User", "Standard user role with basic permissions"),
    ADMIN("Admin", "Administrator role with full system access");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Convert from legacy role names (ROLE_USER, ROLE_ADMIN) to UserRole enum
     */
    public static UserRole fromRoleName(String roleName) {
        if (roleName == null) {
            return USER; // Default to USER
        }
        String normalized = roleName.toUpperCase().replace("ROLE_", "");
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return USER; // Default to USER for unknown roles
        }
    }

    /**
     * Get role name with ROLE_ prefix for Spring Security compatibility
     */
    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}
