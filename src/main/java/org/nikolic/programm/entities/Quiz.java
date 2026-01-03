package org.nikolic.programm.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ FIXED Quiz Entity
 *
 * Added @JsonIgnoreProperties to prevent circular reference issues
 */
@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "category")
    private String category;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "emailVerification", "createdQuizzes", "attempts"})
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_published")
    private Boolean isPublished;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("quiz")
    private List<Question> questions = new ArrayList<>();

    // Constructors
    public Quiz() {
        this.createdAt = LocalDateTime.now();
        this.isPublished = false;
    }

    public Quiz(String title, String description) {
        this();
        this.title = title;
        this.description = description;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    // Helper Methods
    public boolean isPublished() {
        return Boolean.TRUE.equals(isPublished);
    }

    public void publish() {
        this.isPublished = true;
    }

    public void unpublish() {
        this.isPublished = false;
    }

    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuiz(null);
    }

    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", isPublished=" + isPublished +
                ", questionCount=" + getQuestionCount() +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quiz)) return false;
        Quiz quiz = (Quiz) o;
        return id != null && id.equals(quiz.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}