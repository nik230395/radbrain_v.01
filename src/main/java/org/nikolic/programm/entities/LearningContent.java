package org.nikolic.programm.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity für einzelne Content-Blöcke in einem Lernmodul
 * Ähnlich wie Choices im Quiz-System
 */
@Entity
@Table(name = "learning_contents")
public class LearningContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonIgnoreProperties("contents")
    private LearningModule module;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType = ContentType.TEXT;

    @Column(length = 255)
    private String title;

    @Column(name = "content_data", columnDefinition = "LONGTEXT", nullable = false)
    private String contentData;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public LearningContent() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public LearningContent(ContentType contentType, String contentData) {
        this();
        this.contentType = contentType;
        this.contentData = contentData;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LearningModule getModule() {
        return module;
    }

    public void setModule(LearningModule module) {
        this.module = module;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentData() {
        return contentData;
    }

    public void setContentData(String contentData) {
        this.contentData = contentData;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper Methods
    public boolean isText() {
        return contentType == ContentType.TEXT;
    }

    public boolean isHeading() {
        return contentType == ContentType.HEADING;
    }

    public boolean isMedia() {
        return contentType == ContentType.IMAGE ||
                contentType == ContentType.VIDEO ||
                contentType == ContentType.DIAGRAM;
    }

    public int getWordCount() {
        if (contentData == null || contentData.trim().isEmpty()) {
            return 0;
        }
        return contentData.trim().split("\\s+").length;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "LearningContent{" +
                "id=" + id +
                ", contentType=" + contentType +
                ", title='" + title + '\'' +
                ", displayOrder=" + displayOrder +
                ", wordCount=" + getWordCount() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LearningContent)) return false;
        LearningContent that = (LearningContent) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}