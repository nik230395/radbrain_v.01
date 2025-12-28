package org.nikolic.programm.utils;

import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.Quiz;

import java.util.Comparator;
import java.util.stream.Collectors;

public class QuizMapper {

    public static QuizDto toDto(Quiz quiz) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCategory(String.valueOf(quiz.getCategory()));
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setIsPublished(quiz.getIsPublished());

        if (quiz.getQuestions() != null) {
            dto.setQuestions(
                    quiz.getQuestions().stream()
                            .sorted(Comparator.comparing(Question::getPosition,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(QuizMapper::toQuestionDto)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private static QuizDto.QuestionDto toQuestionDto(Question question) {
        QuizDto.QuestionDto dto = new QuizDto.QuestionDto();
        dto.setId(question.getId());
        dto.setQtype(question.getQtype().name());
        dto.setText(question.getText());
        dto.setAuxText(question.getAuxText());
        dto.setPosition(question.getPosition());

        if (question.getChoices() != null) {
            dto.setChoices(
                    question.getChoices().stream()
                            .sorted(Comparator.comparing(Choice::getPosition,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(QuizMapper::toChoiceDto)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private static QuizDto.ChoiceDto toChoiceDto(Choice choice) {
        QuizDto.ChoiceDto dto = new QuizDto.ChoiceDto();
        dto.setId(choice.getId());
        dto.setText(choice.getText());
        dto.setPosition(choice.getPosition());
        // isCorrect wird bewusst NICHT gemappt!
        return dto;
    }
}