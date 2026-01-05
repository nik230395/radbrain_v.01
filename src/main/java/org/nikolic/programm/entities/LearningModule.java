package org.nikolic.programm.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity für Lernmodule/Kapitel innerhalb eines Lernbereichs
 * Ähnlich wie Questions im Quiz-System
 */
@Entity
@Table(name = "learning_modules")
public class LearningModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_area_id", nullable = false)
    @JsonIgnoreProperties("modules")
    private LearningArea learningArea;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(length = 50)
    private String icon;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in Minuten

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("module")
    @OrderBy("displayOrder ASC")
    private List<LearningContent> contents = new ArrayList<>();

    // Constructors
    public LearningModule() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public LearningModule(String title) {
        this();
        this.title = title;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LearningArea getLearningArea() {
        return learningArea;
    }

    public void setLearningArea(LearningArea learningArea) {
        this.learningArea = learningArea;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
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

    public List<LearningContent> getContents() {
        return contents;
    }

    public void setContents(List<LearningContent> contents) {
        this.contents = contents;
    }

    // Helper Methods
    public void addContent(LearningContent content) {
        contents.add(content);
        content.setModule(this);
    }

    public void removeContent(LearningContent content) {
        contents.remove(content);
        content.setModule(null);
    }

    public int getContentCount() {
        return contents != null ? contents.size() : 0;
    }

    public int getTotalWordCount() {
        if (contents == null || contents.isEmpty()) {
            return 0;
        }
        return contents.stream()
                .mapToInt(content -> {
                    String data = content.getContentData();
                    return data != null ? data.split("\\s+").length : 0;
                })
                .sum();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "LearningModule{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", contentCount=" + getContentCount() +
                ", displayOrder=" + displayOrder +
                ", estimatedDuration=" + estimatedDuration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LearningModule)) return false;
        LearningModule that = (LearningModule) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}