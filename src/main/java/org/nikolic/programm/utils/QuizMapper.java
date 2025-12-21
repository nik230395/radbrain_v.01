package org.nikolic.programm.utils;

import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Quiz;

public class QuizMapper {

    public static QuizDto toDto(Quiz quiz) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCategory(quiz.getCategory());
        dto.setIsPublished(quiz.getIsPublished());
        if (quiz.getCreatedBy() != null) {
            dto.setCreatedByEmail(quiz.getCreatedBy().getEmail());
        }
        return dto;
    }
}