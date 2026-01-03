package org.nikolic.programm.entities;

import jakarta.persistence.*;

/**
 * ✅ FIXED AcceptableAnswer Entity
 *
 * Changes:
 * - Fixed naming: answer_text → answerText (camelCase in Java)
 * - Fixed naming: match_mode → matchMode (camelCase in Java)
 * - Proper getter/setter names
 * - Added indexes for performance
 */
@Entity
@Table(name = "acceptable_answers", indexes = {
        @Index(name = "idx_acceptable_answer_question", columnList = "question_id")
})
public class AcceptableAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", nullable = false, length = 500)
    private String answerText;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_mode", nullable = false)
    private MatchMode matchMode = MatchMode.EXACT;

    // Constructors
    public AcceptableAnswer() {
    }

    public AcceptableAnswer(Question question, String answerText, MatchMode matchMode) {
        this.question = question;
        this.answerText = answerText;
        this.matchMode = matchMode;
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

    // ✅ FIXED: Proper Java getter name
    public String getAnswerText() {
        return answerText;
    }

    // ✅ FIXED: Proper Java setter name
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    // ✅ FIXED: Proper Java getter name
    public MatchMode getMatchMode() {
        return matchMode;
    }

    // ✅ FIXED: Proper Java setter name
    public void setMatchMode(MatchMode matchMode) {
        this.matchMode = matchMode;
    }

    // Helper Methods
    public boolean matches(String userInput) {
        if (userInput == null || answerText == null) {
            return false;
        }

        switch (matchMode) {
            case EXACT:
                return userInput.equals(answerText);
            case CASE_INSENSITIVE:
                return userInput.equalsIgnoreCase(answerText);
            case CONTAINS:
                return userInput.toLowerCase().contains(answerText.toLowerCase());
            case REGEX:
                try {
                    return userInput.matches(answerText);
                } catch (Exception e) {
                    return false;
                }
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AcceptableAnswer)) return false;
        AcceptableAnswer that = (AcceptableAnswer) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AcceptableAnswer{" +
                "id=" + id +
                ", answerText='" + answerText + '\'' +
                ", matchMode=" + matchMode +
                '}';
    }
}