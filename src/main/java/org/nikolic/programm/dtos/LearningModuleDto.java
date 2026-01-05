package org.nikolic.programm.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class LearningModuleDto {
    private Long id;
    private Long learningAreaId;
    private String title;
    private String subtitle;
    private String description;
    private Integer displayOrder;
    private String icon;
    private Integer estimatedDuration;
    private Integer contentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LearningContentDto> contents;

    // Constructors
    public LearningModuleDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLearningAreaId() { return learningAreaId; }
    public void setLearningAreaId(Long learningAreaId) { this.learningAreaId = learningAreaId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public Integer getContentCount() { return contentCount; }
    public void setContentCount(Integer contentCount) { this.contentCount = contentCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<LearningContentDto> getContents() { return contents; }
    public void setContents(List<LearningContentDto> contents) { this.contents = contents; }
}