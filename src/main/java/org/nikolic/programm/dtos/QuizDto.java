package org.nikolic.programm.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private List<QuestionDto> questions;
    private Integer questionCount;

    // Private constructor for Builder
    private QuizDto() {}

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Boolean getIsPublished() { return isPublished; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<QuestionDto> getQuestions() {
        return questions != null ? new ArrayList<>(questions) : Collections.emptyList();
    }
    public Integer getQuestionCount() { return questionCount; }

    // Setters (needed for Jackson)
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final QuizDto dto = new QuizDto();

        public Builder id(Long id) {
            dto.id = id;
            return this;
        }

        public Builder title(String title) {
            dto.title = title;
            return this;
        }

        public Builder description(String description) {
            dto.description = description;
            return this;
        }

        public Builder category(String category) {
            dto.category = category;
            return this;
        }

        public Builder isPublished(Boolean isPublished) {
            dto.isPublished = isPublished;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            dto.createdAt = createdAt;
            return this;
        }

        public Builder questions(List<QuestionDto> questions) {
            dto.questions = questions != null ? new ArrayList<>(questions) : new ArrayList<>();
            return this;
        }

        public Builder questionCount(Integer questionCount) {
            dto.questionCount = questionCount;
            return this;
        }

        public QuizDto build() {
            return dto;
        }
    }
}