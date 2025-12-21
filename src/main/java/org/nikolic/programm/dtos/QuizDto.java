package org.nikolic.programm.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private String category; // Added category field
    private Boolean isPublished;
    private String createdByEmail;
    private List<QuestionDto> questions;
}