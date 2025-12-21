package org.nikolic.programm.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQuizRequest {

    private String title;

    private String description;

    private String category; // New field for category

    private Long createdBy; // ID of the user creating the quiz
}