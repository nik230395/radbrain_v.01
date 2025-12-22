package org.nikolic.programm.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "choices")
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "is_correct")
    private Boolean is_correct = false;

    @Column(name = "position")
    private Integer position = 0;

    // Constructors
    public Choice() {
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getIs_correct() {
        return is_correct;
    }

    public void setIs_correct(Boolean is_correct) {
        this.is_correct = is_correct;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}