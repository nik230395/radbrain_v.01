package org.nikolic.programm.utils;

import org.nikolic.programm. dtos. ChoiceDto;
import org.nikolic.programm.dtos. QuestionDto;
import org. nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.entities.Question;
import org.nikolic. programm.entities.Quiz;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuizMapper {

    /**
     * Convert Quiz entity to QuizDto (public view - no sensitive data)
     */
    public static QuizDto toDto(Quiz quiz) {
        if (quiz == null) {
            return null;
        }

        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setIsPublished(quiz.getIsPublished());
        dto.setCreatedAt(quiz.getCreatedAt());

        // Safely handle category
        if (quiz.getCategory() != null) {
            dto.setCategory(quiz.getCategory().getName());
        }

        // Safely handle creator
        if (quiz.getCreatedBy() != null) {
            dto.setCreatedByEmail(quiz.getCreatedBy().getEmail());
        }

        // Convert questions with choices
        if (quiz.getQuestions() != null) {
            List<QuestionDto> questionDtos = quiz.getQuestions().stream()
                    .map(QuizMapper::toQuestionDto)
                    .collect(Collectors.toList());
            dto.setQuestions(questionDtos);
            dto.setQuestionCount(questionDtos.size());
        } else {
            dto.setQuestions(Collections.emptyList());
            dto.setQuestionCount(0);
        }

        return dto;
    }

    /**
     * Convert Quiz entity to QuizDto (admin view - includes unpublished status)
     */
    public static QuizDto toAdminDto(Quiz quiz) {
        if (quiz == null) {
            return null;
        }

        QuizDto dto = toDto(quiz); // Start with basic conversion

        // Admin can see additional details
        // (Currently same as public, but can be extended)

        return dto;
    }

    /**
     * Convert Question entity to QuestionDto
     */
    public static QuestionDto toQuestionDto(Question question) {
        if (question == null) {
            return null;
        }

        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setQtype(question.getQtype() != null ? question.getQtype().name() : null);
        dto.setText(question.getText());
        dto.setAuxText(question. getAuxText());
        dto.setPosition(question.getPosition());

        // Convert choices (without exposing correct answers for security)
        if (question.getChoices() != null) {
            List<ChoiceDto> choiceDtos = question.getChoices().stream()
                    .map(QuizMapper::toChoiceDto)
                    .collect(Collectors.toList());
            dto.setChoices(choiceDtos);
        } else {
            dto.setChoices(Collections.emptyList());
        }

        return dto;
    }

    /**
     * Convert Question entity to QuestionDto (admin view - includes correct answers)
     */
    public static QuestionDto toAdminQuestionDto(Question question) {
        if (question == null) {
            return null;
        }

        QuestionDto dto = toQuestionDto(question); // Start with basic conversion

        // Admin can see correct answers
        if (question.getChoices() != null) {
            List<ChoiceDto> choiceDtos = question.getChoices().stream()
                    .map(QuizMapper::toAdminChoiceDto)
                    .collect(Collectors.toList());
            dto.setChoices(choiceDtos);
        }

        return dto;
    }

    /**
     * Convert Choice entity to ChoiceDto (public view - no correct answer exposed)
     */
    public static ChoiceDto toChoiceDto(Choice choice) {
        if (choice == null) {
            return null;
        }

        ChoiceDto dto = new ChoiceDto();
        dto.setId(choice.getId());
        dto.setText(choice.getText());
        dto.setPosition(choice.getPosition());
        // Intentionally NOT setting isCorrect for security

        return dto;
    }

    /**
     * Convert Choice entity to ChoiceDto (admin view - includes correct answer)
     */
    public static ChoiceDto toAdminChoiceDto(Choice choice) {
        if (choice == null) {
            return null;
        }

        ChoiceDto dto = toChoiceDto(choice); // Start with basic conversion
        // Admin can see correct answers - you'd need to add isCorrect field to ChoiceDto for admin use
        // Or create a separate AdminChoiceDto

        return dto;
    }

    /**
     * Convert Quiz entity to QuizDto without questions (for list views)
     */
    public static QuizDto toSummaryDto(Quiz quiz) {
        if (quiz == null) {
            return null;
        }

        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setIsPublished(quiz.getIsPublished());
        dto.setCreatedAt(quiz.getCreatedAt());

        // Safely handle category
        if (quiz.getCategory() != null) {
            dto.setCategory(quiz.getCategory().getName());
        }

        // Safely handle creator
        if (quiz.getCreatedBy() != null) {
            dto.setCreatedByEmail(quiz.getCreatedBy().getEmail());
        }

        // Set question count without loading full questions
        if (quiz.getQuestions() != null) {
            dto.setQuestionCount(quiz.getQuestions().size());
        } else {
            dto.setQuestionCount(0);
        }

        // Don't set questions to avoid lazy loading in list views
        dto.setQuestions(Collections.emptyList());

        return dto;
    }

    /**
     * Bulk conversion for quiz lists
     */
    public static List<QuizDto> toDtoList(List<Quiz> quizzes) {
        if (quizzes == null) {
            return Collections.emptyList();
        }

        return quizzes.stream()
                .map(QuizMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Bulk conversion for quiz lists (with full details)
     */
    public static List<QuizDto> toDetailedDtoList(List<Quiz> quizzes) {
        if (quizzes == null) {
            return Collections.emptyList();
        }

        return quizzes.stream()
                .map(QuizMapper::toDto)
                .collect(Collectors.toList());
    }
}