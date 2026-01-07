package org.nikolic.programm.utils;

import org.nikolic.programm.dtos.ChoiceDto;
import org.nikolic.programm.dtos.QuestionDto;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuizMapper {

    private static final Logger logger = LoggerFactory.getLogger(QuizMapper.class);

    // Private constructor to prevent instantiation
    private QuizMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts Quiz entity to QuizDto with full validation
     *
     * @param quiz The quiz entity to convert
     * @return QuizDto or null if input is null
     * @throws IllegalStateException if quiz is in invalid state
     */
    public static QuizDto toDto(Quiz quiz) {
        if (quiz == null) {
            logger.warn("Attempted to convert null Quiz to DTO");
            return null;
        }

        try {
            return QuizDto.builder()
                    .id(quiz.getId())
                    .title(validateAndGet(quiz.getTitle(), "Untitled Quiz"))
                    .description(quiz.getDescription())
                    .category(quiz.getCategory())
                    .isPublished(Optional.ofNullable(quiz.isPublished()).orElse(false))
                    .createdAt(quiz.getCreatedAt())
                    .questions(mapQuestions(quiz.getQuestions()))
                    .questionCount(getQuestionCount(quiz.getQuestions()))
                    .build();

        } catch (Exception e) {
            logger.error("Error converting Quiz {} to DTO: {}", quiz.getId(), e.getMessage(), e);
            throw new IllegalStateException("Failed to convert Quiz to DTO", e);
        }
    }

    /**
     * Converts Quiz entity to lightweight DTO (without questions)
     * Useful for list views where questions aren't needed
     */
    public static QuizDto toLightDto(Quiz quiz) {
        if (quiz == null) {
            return null;
        }

        return QuizDto.builder()
                .id(quiz.getId())
                .title(validateAndGet(quiz.getTitle(), "Untitled Quiz"))
                .description(quiz.getDescription())
                .category(quiz.getCategory())
                .isPublished(Optional.ofNullable(quiz.isPublished()).orElse(false))
                .createdAt(quiz.getCreatedAt())
                .questions(Collections.emptyList())
                .questionCount(getQuestionCount(quiz.getQuestions()))
                .build();
    }

    /**
     * Converts Question entity to QuestionDto with validation
     */
    public static QuestionDto questionToDto(Question question) {
        if (question == null) {
            logger.warn("Attempted to convert null Question to DTO");
            return null;
        }

        try {
            return QuestionDto.builder()
                    .id(question.getId())
                    .text(validateAndGet(question.getText(), ""))
                    .qtype(question.getQtype() != null ? question.getQtype().name() : "SINGLE")
                    .auxText(question.getAuxText())
                    .position(Optional.ofNullable(question.getPosition()).orElse(0))
                    .choices(mapChoices(question.getChoices()))
                    .build();

        } catch (Exception e) {
            logger.error("Error converting Question {} to DTO: {}", question.getId(), e.getMessage());
            return createEmptyQuestionDto(question.getId());
        }
    }

    /**
     * Converts Choice entity to ChoiceDto with validation
     */
    public static ChoiceDto choiceToDto(Choice choice) {
        if (choice == null) {
            logger.warn("Attempted to convert null Choice to DTO");
            return null;
        }

        try {
            return ChoiceDto.builder()
                    .id(choice.getId())
                    .text(validateAndGet(choice.getText(), ""))
                    .position(Optional.ofNullable(choice.getPosition()).orElse(0))
                    .isCorrect(Optional.ofNullable(choice.getIsCorrect()).orElse(false))
                    .build();

        } catch (Exception e) {
            logger.error("Error converting Choice {} to DTO: {}", choice.getId(), e.getMessage());
            return createEmptyChoiceDto(choice.getId());
        }
    }

    /**
     * Bulk conversion with null filtering and error recovery
     */
    public static List<QuizDto> toDtoList(List<Quiz> quizzes) {
        if (quizzes == null || quizzes.isEmpty()) {
            return Collections.emptyList();
        }

        return quizzes.stream()
                .filter(quiz -> quiz != null)
                .map(QuizMapper::toDto)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Bulk conversion to lightweight DTOs
     */
    public static List<QuizDto> toLightDtoList(List<Quiz> quizzes) {
        if (quizzes == null || quizzes.isEmpty()) {
            return Collections.emptyList();
        }

        return quizzes.stream()
                .filter(quiz -> quiz != null)
                .map(QuizMapper::toLightDto)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
    /**
     * Safely maps questions list with null protection
     */
    private static List<QuestionDto> mapQuestions(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }

        return questions.stream()
                .filter(q -> q != null)
                .map(QuizMapper::questionToDto)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Safely maps choices list with null protection
     */
    private static List<ChoiceDto> mapChoices(List<Choice> choices) {
        if (choices == null || choices.isEmpty()) {
            return Collections.emptyList();
        }

        return choices.stream()
                .filter(c -> c != null)
                .map(QuizMapper::choiceToDto)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Gets question count with null safety
     */
    private static int getQuestionCount(List<Question> questions) {
        return questions != null ? questions.size() : 0;
    }

    /**
     * Validates and returns value or default
     */
    private static String validateAndGet(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    /**
     * Creates empty QuestionDto for error recovery
     */
    private static QuestionDto createEmptyQuestionDto(Long id) {
        return QuestionDto.builder()
                .id(id)
                .text("Error loading question")
                .qtype("SINGLE")
                .choices(Collections.emptyList())
                .build();
    }

    /**
     * Creates empty ChoiceDto for error recovery
     */
    private static ChoiceDto createEmptyChoiceDto(Long id) {
        return ChoiceDto.builder()
                .id(id)
                .text("Error loading choice")
                .position(0)
                .isCorrect(false)
                .build();
    }

    /**
     * Validates that a Quiz entity is in valid state for conversion
     */
    private static void validateQuizEntity(Quiz quiz) {
        if (quiz.getId() == null) {
            throw new IllegalStateException("Quiz ID cannot be null");
        }
        if (quiz.getTitle() == null || quiz.getTitle().trim().isEmpty()) {
            logger.warn("Quiz {} has no title", quiz.getId());
        }
    }
}