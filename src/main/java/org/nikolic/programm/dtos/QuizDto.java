package org.nikolic.programm.dtos;

import lombok.Data;
import org.nikolic.programm.entities.Quiz;

@Data
public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private String category; // Speichert nur den Namen der Kategorie

    // Konstruktor oder Builder
    public QuizDto() {
    }

    public QuizDto(Quiz quiz) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.description = quiz.getDescription();
        this.category = quiz.getCategory() != null ? quiz.getCategory().getName() : null;
    }

    // Getter und Setter
}