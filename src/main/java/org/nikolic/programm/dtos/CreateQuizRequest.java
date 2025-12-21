package org.nikolic.programm.dtos;

import lombok.Data;
import org.nikolic.programm.entities.QuizCategory;

@Data
public class CreateQuizRequest {

    private String title;

    private String description;

    private QuizCategory category; // New field for category

    private Long createdBy; // ID of the user creating the quiz
}