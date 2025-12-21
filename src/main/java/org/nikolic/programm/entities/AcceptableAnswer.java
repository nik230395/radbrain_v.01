package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "acceptable_answers")
public class AcceptableAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Antwort-ID

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    private Question question; // Zugehörige Frage

    @Column(name = "answer_text", nullable = false, length = 500)
    private String answerText; // Der als richtig akzeptierte Text

    @Enumerated(EnumType.STRING)
    @Column(name = "match_mode", nullable = false)
    private MatchMode matchMode; // Modus für Antwortvergleich (z.B. exakt, case-insensitiv, etc.)
}