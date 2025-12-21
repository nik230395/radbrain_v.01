package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.nikolic.programm.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // User-ID

    @Column(unique = true, nullable = false)
    private String email; // Login-E-Mail-Adresse

    // explizite Spalten-Mapping, damit es mit deinem SQL-Dump passt
    @Column(name = "password_hash", nullable = false)
    private String password_hash; // Verschlüsseltes Passwort

    @Column(name = "full_name")
    private String fullname; // Name des Benutzers (mapped to full_name)

    @Column(name = "is_active")
    private Boolean is_active; // Ist der Account aktiv/sichtbar

    @Column(name = "created_at")
    private LocalDateTime created_at; // Registriert am

    @OneToMany(mappedBy = "createdBy")
    private List<Quiz> createdQuizzes; // Vom User erstelle Quizzes

    // Use UserRole enum instead of Set<Role> for better performance and simplicity
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole = UserRole.USER; // Default to USER role

    public User() {}

    // Utility methods for checking user roles
    public boolean isAdmin() {
        return this.userRole == UserRole.ADMIN;
    }

    public boolean isUser() {
        return this.userRole == UserRole.USER;
    }

    public boolean hasRole(UserRole role) {
        return this.userRole == role;
    }

    /**
     * Get role name with ROLE_ prefix for Spring Security compatibility
     */
    public String getRoleNameForSecurity() {
        return userRole != null ? userRole.getRoleName() : UserRole.USER.getRoleName();
    }
}