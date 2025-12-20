package org.nikolic.programm.dtos;

import lombok.Data;

@Data
public class CreateQuizRequest {

    private String title;

    private String description;

    private String category; // New field for category

    private Long createdBy; // ID of the user creating the quiz
}