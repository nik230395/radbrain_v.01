package org.nikolic.programm.utils;

import org.nikolic.programm.dtos.ChoiceDto;
import org.nikolic.programm.dtos.QuestionDto;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.Quiz;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ FIXED QuizMapper
 * Now includes isCorrect field in ChoiceDto!
 */
public class QuizMapper {

    public static QuizDto toDto(Quiz quiz) {
        if (quiz == null) return null;

        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCategory(quiz.getCategory());
        dto.setIsPublished(quiz.isPublished());
        dto.setCreatedAt(quiz.getCreatedAt());

        // Map questions
        if (quiz.getQuestions() != null) {
            List<QuestionDto> questionDtos = quiz.getQuestions().stream()
                    .map(QuizMapper::questionToDto)
                    .collect(Collectors.toList());
            dto.setQuestions(questionDtos);
            dto.setQuestionCount(questionDtos.size());
        } else {
            dto.setQuestions(Collections.emptyList());
            dto.setQuestionCount(0);
        }

        return dto;
    }

    public static QuestionDto questionToDto(Question question) {
        if (question == null) return null;

        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setText(question.getText());
        dto.setQtype(question.getQtype().name());
        dto.setAuxText(question.getAuxText());
        dto.setPosition(question.getPosition());

        // Map choices - INCLUDING isCorrect!
        if (question.getChoices() != null) {
            List<ChoiceDto> choiceDtos = question.getChoices().stream()
                    .map(QuizMapper::choiceToDto)
                    .collect(Collectors.toList());
            dto.setChoices(choiceDtos);
        }

        return dto;
    }

    public static ChoiceDto choiceToDto(Choice choice) {
        if (choice == null) return null;

        ChoiceDto dto = new ChoiceDto();
        dto.setId(choice.getId());
        dto.setText(choice.getText());
        dto.setPosition(choice.getPosition());

        // ✅ CRITICAL FIX: Include isCorrect field!
        dto.setIsCorrect(choice.getIsCorrect());

        return dto;
    }
}