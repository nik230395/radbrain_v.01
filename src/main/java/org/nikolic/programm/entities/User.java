package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // explizites Mapping zum role-Feld in der users-Tabelle
    @Column(name = "role")
    private String role; // Rolle des Benutzers (z.B. ADMIN, USER)

    @ManyToMany(fetch = FetchType.EAGER) // kein Cascade: rollen separat verwalten
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {}

    // Hilfsmethoden zum Verwalten der Rollen
    public void addRole(Role role) {
        if (role == null) return;
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        if (role == null) return;
        this.roles.remove(role);
    }
}