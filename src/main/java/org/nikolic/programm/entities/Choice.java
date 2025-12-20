package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity
@Table(name = "choices")
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Antwort-ID

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    private Question question; // Zugehörige Frage

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text; // Antworttext

    @Column(name = "is_correct")
    private Boolean isCorrect; // Ist diese Antwort korrekt?

    private Integer position; // Position unter den Auswahlmöglichkeiten
}