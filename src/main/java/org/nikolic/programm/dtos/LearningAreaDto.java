package org.nikolic.programm.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class LearningAreaDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String subtitle;
    private String iconClass;
    private String colorTheme;
    private Integer displayOrder;
    private Boolean isPublished;
    private Integer moduleCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LearningModuleDto> modules;

    // Constructors
    public LearningAreaDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public String getColorTheme() { return colorTheme; }
    public void setColorTheme(String colorTheme) { this.colorTheme = colorTheme; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }

    public Integer getModuleCount() { return moduleCount; }
    public void setModuleCount(Integer moduleCount) { this.moduleCount = moduleCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<LearningModuleDto> getModules() { return modules; }
    public void setModules(List<LearningModuleDto> modules) { this.modules = modules; }
}