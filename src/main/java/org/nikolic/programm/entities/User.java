package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
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

    // User role using enum
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole userRole = UserRole.USER; // Default role is USER

    public User() {}

    // Getter and setter for userRole
    public UserRole getUserRole() {
        return this.userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    // Helper method to check if user is admin
    public boolean isAdmin() {
        return this.userRole == UserRole.ADMIN;
    }

    // Helper method to get role name as String (for backward compatibility)
    public String getRole() {
        return this.userRole != null ? this.userRole.getRoleName() : UserRole.USER.getRoleName();
    }

    // Helper method to set role from String (for backward compatibility)
    public void setRole(String roleName) {
        this.userRole = UserRole.fromString(roleName);
    }
}