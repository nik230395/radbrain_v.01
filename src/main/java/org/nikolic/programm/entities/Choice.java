package org.nikolic.programm.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * ✅ FIXED Choice Entity
 *
 * Changes:
 * - Fixed naming: is_correct → isCorrect (camelCase in Java)
 * - Added @JsonIgnoreProperties to prevent circular references
 * - Proper getter/setter names
 * - Added validation
 * - Added indexes for performance
 */
@Entity
@Table(name = "choices", indexes = {
        @Index(name = "idx_choice_question", columnList = "question_id"),
        @Index(name = "idx_choice_position", columnList = "question_id, position")
})
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"choices", "acceptableAnswers", "quiz"})
    private Question question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @Column(name = "position")
    private Integer position = 0;

    // Constructors
    public Choice() {
    }

    public Choice(Question question, String text, Boolean isCorrect, Integer position) {
        this.question = question;
        this.text = text;
        this.isCorrect = isCorrect;
        this.position = position;
    }

    // Getters and Setters - PROPER JAVA NAMING
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

    // ✅ FIXED: Proper Java getter name
    public Boolean getIsCorrect() {
        return isCorrect;
    }

    // ✅ FIXED: Proper Java setter name
    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    // ✅ ADDED: Convenience method (standard Java bean pattern)
    public Boolean isCorrect() {
        return isCorrect;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    // Helper Methods
    public boolean hasCorrectAnswer() {
        return Boolean.TRUE.equals(isCorrect);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Choice)) return false;
        Choice choice = (Choice) o;
        return id != null && id.equals(choice.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Choice{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", isCorrect=" + isCorrect +
                ", position=" + position +
                '}';
    }
}