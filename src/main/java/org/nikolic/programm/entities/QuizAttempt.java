package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @ToString.Exclude
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(name = "score_pct", precision = 5, scale = 2)
    private BigDecimal scorePct;

    @Column(columnDefinition = "LONGTEXT")
    private String answersJson;

    // Optional fields
    @Column(length = 1000)
    private String feedback; // Feedback from the user

    private Integer attemptCount; // Number of attempts made
}