package org.nikolic.programm.dtos;

import lombok.Data;

import java.util.List;

@Data
public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Boolean isPublished;
    private String createdByEmail;
    private List<QuestionDto> questions;
}