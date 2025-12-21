package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Choice Entity (Multiple Choice Antwort-Option)
 * ✅ FIX: Field names passen zur DB (snake_case)
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "choices")
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    private Question question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    // ✅ FIX: is_correct in DB
    @Column(name = "is_correct")
    private Boolean is_correct = false; // Verwende snake_case wie DB

    @Column(name = "position")
    private Integer position = 0;
}