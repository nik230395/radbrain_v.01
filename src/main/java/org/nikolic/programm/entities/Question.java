package org.nikolic.programm.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

/**
 * ✅ FIXED Question Entity
 *
 * Added @JsonIgnoreProperties to prevent circular reference issues
 * when serializing to JSON
 */
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnoreProperties({"questions", "createdBy", "attempts"})
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType qtype;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "aux_text", columnDefinition = "TEXT")
    private String auxText;

    private Integer position;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("question")
    private List<Choice> choices;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("question")
    private List<AcceptableAnswer> acceptableAnswers;

    // Constructors
    public Question() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public QuestionType getQtype() {
        return qtype;
    }

    public void setQtype(QuestionType qtype) {
        this.qtype = qtype;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuxText() {
        return auxText;
    }

    public void setAuxText(String auxText) {
        this.auxText = auxText;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public List<AcceptableAnswer> getAcceptableAnswers() {
        return acceptableAnswers;
    }

    public void setAcceptableAnswers(List<AcceptableAnswer> acceptableAnswers) {
        this.acceptableAnswers = acceptableAnswers;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", qtype=" + qtype +
                ", text='" + text + '\'' +
                ", position=" + position +
                '}';
    }
}