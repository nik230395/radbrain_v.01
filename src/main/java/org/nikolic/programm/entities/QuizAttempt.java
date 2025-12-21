package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Versuch-ID

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @ToString.Exclude
    private Quiz quiz; // Das Quiz, das gespielt wurde

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user; // Der teilnehmende Benutzer

    private LocalDateTime startedAt; // Beginn des Versuchs

    private LocalDateTime completedAt; // Abschluss des Versuchs

    @Column(name = "score_pct", precision = 5, scale = 2)
    private BigDecimal scorePct; // Prozentzahl der richtigen Antworten

    @Column(columnDefinition = "LONGTEXT")
    private String answersJson; // Antworten als JSON (für spätere Auswertung)
}