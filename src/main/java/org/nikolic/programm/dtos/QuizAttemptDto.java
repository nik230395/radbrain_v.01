package org.nikolic.programm. dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuizAttemptDto {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private String userEmail;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private BigDecimal scorePct;
    private Integer correctAnswers;
    private Integer totalQuestions;

    // Constructors
    public QuizAttemptDto() {}

    public QuizAttemptDto(Long id, Long quizId, String quizTitle, String userEmail,
                          LocalDateTime startedAt, LocalDateTime completedAt,
                          BigDecimal scorePct, Integer correctAnswers, Integer totalQuestions) {
        this.id = id;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.userEmail = userEmail;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.scorePct = scorePct;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public BigDecimal getScorePct() { return scorePct; }
    public void setScorePct(BigDecimal scorePct) { this.scorePct = scorePct; }

    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
}