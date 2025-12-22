package org.nikolic.programm.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "acceptable_answers")
public class AcceptableAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", nullable = false, length = 500)
    private String answer_text;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_mode", nullable = false)
    private MatchMode match_mode = MatchMode.EXACT;

    // Constructors
    public AcceptableAnswer() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getAnswer_text() {
        return answer_text;
    }

    public void setAnswer_text(String answer_text) {
        this.answer_text = answer_text;
    }

    public MatchMode getMatch_mode() {
        return match_mode;
    }

    public void setMatch_mode(MatchMode match_mode) {
        this.match_mode = match_mode;
    }
}