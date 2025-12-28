package org.nikolic.programm.dtos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * QuizDto - Ohne Lombok
 *
 * Alle Getters/Setters manuell implementiert
 */
public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Boolean isPublished;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private List<QuestionDto> questions = new ArrayList<>();
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

    // Inner class QuestionDto
    public static class QuestionDto {
        private Long id;
        private String qtype;
        private String text;
        private String auxText;
        private Integer position;
        private List<ChoiceDto> choices = new ArrayList<>();

        // Constructors
        public QuestionDto() {}

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getQtype() {
            return qtype;
        }

        public void setQtype(String qtype) {
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

        public List<ChoiceDto> getChoices() {
            return choices;
        }

        public void setChoices(List<ChoiceDto> choices) {
            this.choices = choices;
        }
    }

    // Inner class ChoiceDto
    public static class ChoiceDto {
        private Long id;
        private String text;
        private Integer position;

        // Constructors
        public ChoiceDto() {}

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }
    }
}