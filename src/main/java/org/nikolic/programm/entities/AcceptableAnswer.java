package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * AcceptableAnswer Entity
 * ✅ FIX: Field names passen jetzt zur DB (snake_case)
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "acceptable_answers")
public class AcceptableAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    private Question question;

    // ✅ FIX: answer_text in DB, aber answerText in Java (JPA macht auto-mapping)
    // ODER explizit: @Column(name = "answer_text")
    @Column(name = "answer_text", nullable = false, length = 500)
    private String answer_text; // Verwende snake_case wie DB

    // ✅ FIX: match_mode in DB
    @Enumerated(EnumType.STRING)
    @Column(name = "match_mode", nullable = false)
    private MatchMode match_mode = MatchMode.EXACT; // Verwende snake_case wie DB
}