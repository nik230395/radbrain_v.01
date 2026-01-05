package org.nikolic.programm.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity für Lernbereiche (z.B. Röntgen, CT, MRT, Ultraschall)
 * Ähnliche Struktur wie Quiz für Konsistenz
 */
@Entity
@Table(name = "learning_areas")
public class LearningArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String subtitle;

    @Column(name = "icon_class", length = 50)
    private String iconClass;

    @Column(name = "color_theme", length = 50)
    private String colorTheme = "#3b82f6";

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"passwordHash", "emailVerification", "createdQuizzes", "attempts"})
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "learningArea", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("learningArea")
    @OrderBy("displayOrder ASC")
    private List<LearningModule> modules = new ArrayList<>();

    // Constructors
    public LearningArea() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public LearningArea(String name, String slug) {
        this();
        this.name = name;
        this.slug = slug;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public String getColorTheme() {
        return colorTheme;
    }

    public void setColorTheme(String colorTheme) {
        this.colorTheme = colorTheme;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<LearningModule> getModules() {
        return modules;
    }

    public void setModules(List<LearningModule> modules) {
        this.modules = modules;
    }

    // Helper Methods
    public boolean isPublished() {
        return Boolean.TRUE.equals(isPublished);
    }

    public void publish() {
        this.isPublished = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void unpublish() {
        this.isPublished = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void addModule(LearningModule module) {
        modules.add(module);
        module.setLearningArea(this);
    }

    public void removeModule(LearningModule module) {
        modules.remove(module);
        module.setLearningArea(null);
    }

    public int getModuleCount() {
        return modules != null ? modules.size() : 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "LearningArea{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", isPublished=" + isPublished +
                ", moduleCount=" + getModuleCount() +
                ", displayOrder=" + displayOrder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LearningArea)) return false;
        LearningArea that = (LearningArea) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}