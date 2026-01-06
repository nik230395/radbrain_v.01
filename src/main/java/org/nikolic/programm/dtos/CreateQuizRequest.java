package org.nikolic.programm.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateQuizRequest {

    @NotBlank(message = "Quiz-Titel ist erforderlich")
    @Size(min = 3, max = 255, message = "Quiz-Titel muss zwischen 3 und 255 Zeichen lang sein")
    private String title;

    @Size(max = 1000, message = "Beschreibung darf maximal 1000 Zeichen lang sein")
    private String description;

    @Size(max = 50, message = "Kategorie darf maximal 50 Zeichen lang sein")
    private String category;

    private boolean isPublished;

    // Standard-Konstruktor (wichtig für Jackson/JSON)
    public CreateQuizRequest() {
    }

    // Konstruktor mit Feldern
    public CreateQuizRequest(String title, String description, String category, boolean isPublished) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.isPublished = isPublished;
    }

    // Getter und Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // WICHTIG für Jackson: Getter/Setter Namen bei "is"-Booleans
    public boolean isIsPublished() { return isPublished; }
    public void setIsPublished(boolean published) { this.isPublished = published; }

    @Override
    public String toString() {
        return "CreateQuizRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", isPublished=" + isPublished +
                '}';
    }
}