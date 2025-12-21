package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Entity - verwendet ENUM für Rollen (einfachste Lösung)
 * Passt zur bestehenden DB-Struktur mit role ENUM('user','admin')
 */
@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password_hash;

    @Column(name = "full_name")
    private String fullname;

    @Column(name = "is_active")
    private Boolean is_active;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    // ENUM Role - passt zur DB: enum('user','admin')
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER; // keine klasse UserRole sondern nur Role

    @OneToMany(mappedBy = "createdBy")
    private List<Quiz> createdQuizzes;

    public User() {
        this.is_active = true;
        this.created_at = LocalDateTime.now();
        this.role = UserRole.USER;
    }

    // Helper methods für Role-Checks
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isUser() {
        return this.role == UserRole.USER;
    }

    public String getRoleAsString() {
        return "ROLE_" + this.role.name();
    }
}