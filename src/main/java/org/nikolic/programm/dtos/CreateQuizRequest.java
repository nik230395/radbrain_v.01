package org.nikolic.programm.dtos;

public class CreateQuizRequest {

    private String title;
    private String description;
    private Long categoryId;

    // Constructors
    public CreateQuizRequest() {}

    public CreateQuizRequest(String title, String description, Long categoryId) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
    }

    // Getters and Setters
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}