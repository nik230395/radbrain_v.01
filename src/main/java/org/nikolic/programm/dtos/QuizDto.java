package org.nikolic.programm.dtos;

import java.time.LocalDateTime;
import java. util.List;

public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Boolean isPublished;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private List<QuestionDto> questions;
    private Integer questionCount;

    // Constructors
    public QuizDto() {}

    public QuizDto(Long id, String title, String description, String category,
                   Boolean isPublished, String createdByEmail, LocalDateTime createdAt,
                   List<QuestionDto> questions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.isPublished = isPublished;
        this.createdByEmail = createdByEmail;
        this.createdAt = createdAt;
        this.questions = questions;
        this.questionCount = questions != null ? questions.size() : 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<QuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDto> questions) {
        this.questions = questions;
        this.questionCount = questions != null ? questions.size() : 0;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
}