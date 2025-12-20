package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "password_reset_codes")
public class PasswordResetCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Welcher Nutzer? (Beziehung zu User)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    // Der Reset-Code, z.B. sechsstellig als String
    @Column(nullable = false, length = 10)
    private String code;

    // Wann erzeugt?
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Schon benutzt?
    @Column(name = "used", nullable = false)
    private Boolean used = false;
}