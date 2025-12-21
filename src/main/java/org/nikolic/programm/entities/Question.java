package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Frage-ID

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @ToString.Exclude
    private Quiz quiz; // Zugehöriges Quiz

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType qtype; // Fragetyp (SINGLE, MULTIPLE, ...)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text; // Text der Frage

    @Column(name = "aux_text", columnDefinition = "TEXT")
    private String auxText; // Optionaler Erklär- oder Hilfetext

    private Integer position; // Position/Sortierreihenfolge im Quiz

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Choice> choices; // Antwortmöglichkeiten (Multiple Choice, etc.)

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<AcceptableAnswer> acceptableAnswers; // Akzeptierte Lösungen (Freitext/Lückentext)
}