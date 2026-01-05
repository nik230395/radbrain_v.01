package org.nikolic.programm.dtos;

import java.time.LocalDateTime;

public class LearningContentDto {
    private Long id;
    private Long moduleId;
    private String contentType;
    private String title;
    private String contentData;
    private Integer displayOrder;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public LearningContentDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentData() { return contentData; }
    public void setContentData(String contentData) { this.contentData = contentData; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

