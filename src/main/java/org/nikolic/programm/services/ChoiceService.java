package org.nikolic.programm.services;

import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.entities.Question;
import org.nikolic.programm.repositories.ChoiceRepository;
import org.nikolic.programm.repositories.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ✅ FIXED ChoiceService
 *
 * Changes:
 * - Updated to use new camelCase getters/setters
 * - Added proper error handling
 * - Added validation
 * - Fixed lambda issues with final variables
 */
@Service
@Transactional
public class ChoiceService {

    private static final Logger logger = LoggerFactory.getLogger(ChoiceService.class);

    private final ChoiceRepository choiceRepository;
    private final QuestionRepository questionRepository;

    public ChoiceService(ChoiceRepository choiceRepository, QuestionRepository questionRepository) {
        this.choiceRepository = choiceRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Create new choice
     */
    public Choice createChoice(Long questionId, String text, Boolean isCorrect) {
        logger.debug("Creating choice for question {}", questionId);

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Choice text cannot be empty");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        Integer maxPosition = choiceRepository.findMaxPositionByQuestionId(questionId);
        if (maxPosition == null) {
            maxPosition = 0;
        }

        Choice choice = new Choice();
        choice.setQuestion(question);
        choice.setText(text.trim());
        choice.setIsCorrect(isCorrect != null ? isCorrect : false);  // ✅ FIXED: Use camelCase method
        choice.setPosition(maxPosition + 1);

        Choice saved = choiceRepository.save(choice);
        logger.info("Created choice {} for question {}", saved.getId(), questionId);

        return saved;
    }

    /**
     * Get choices for a question
     */
    @Transactional(readOnly = true)
    public List<Choice> getChoicesByQuestion(Long questionId) {
        logger.debug("Fetching choices for question {}", questionId);
        return choiceRepository.findByQuestionIdOrderByPositionAsc(questionId);
    }

    /**
     * Delete choice
     */
    public void deleteChoice(Long id) {
        logger.debug("Deleting choice {}", id);

        if (!choiceRepository.existsById(id)) {
            throw new IllegalArgumentException("Choice not found with id: " + id);
        }

        choiceRepository.deleteById(id);
        logger.info("Deleted choice {}", id);
    }

    /**
     * Update choice
     */
    public Choice updateChoice(Long id, String text, Boolean isCorrect) {
        logger.debug("Updating choice {}", id);

        Choice choice = choiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Choice not found with id: " + id));

        if (text != null && !text.trim().isEmpty()) {
            choice.setText(text.trim());
        }

        if (isCorrect != null) {
            choice.setIsCorrect(isCorrect);  // ✅ FIXED: Use camelCase method
        }

        Choice updated = choiceRepository.save(choice);
        logger.info("Updated choice {}", id);

        return updated;
    }

    /**
     * Get correct choices for a question
     */
    @Transactional(readOnly = true)
    public List<Choice> getCorrectChoicesByQuestion(Long questionId) {
        logger.debug("Fetching correct choices for question {}", questionId);
        return choiceRepository.findByQuestionIdAndIsCorrectOrderByPositionAsc(questionId, true);
    }

    /**
     * Get incorrect choices for a question
     */
    @Transactional(readOnly = true)
    public List<Choice> getIncorrectChoicesByQuestion(Long questionId) {
        logger.debug("Fetching incorrect choices for question {}", questionId);
        return choiceRepository.findByQuestionIdAndIsCorrectOrderByPositionAsc(questionId, false);
    }

    /**
     * Check if question has correct answers
     */
    @Transactional(readOnly = true)
    public boolean questionHasCorrectAnswers(Long questionId) {
        long count = choiceRepository.countByQuestionIdAndIsCorrect(questionId, true);
        return count > 0;
    }

    /**
     * Reorder choices for a question
     * ✅ FIXED: Removed lambda issues with effectively final variables
     */
    public void reorderChoices(Long questionId, List<Long> choiceIds) {
        logger.debug("Reordering {} choices for question {}", choiceIds.size(), questionId);

        if (choiceIds == null || choiceIds.isEmpty()) {
            throw new IllegalArgumentException("Choice IDs list cannot be empty");
        }

        // Use traditional for loop to avoid lambda/final variable issues
        for (int i = 0; i < choiceIds.size(); i++) {
            Long choiceId = choiceIds.get(i);
            int newPosition = i + 1;

            Choice choice = choiceRepository.findById(choiceId)
                    .orElseThrow(() -> new IllegalArgumentException("Choice not found with id: " + choiceId));

            // Verify choice belongs to the correct question
            if (!choice.getQuestion().getId().equals(questionId)) {
                throw new IllegalArgumentException("Choice " + choiceId + " does not belong to question " + questionId);
            }

            choice.setPosition(newPosition);
            choiceRepository.save(choice);
        }

        logger.info("Reordered {} choices for question {}", choiceIds.size(), questionId);
    }

    /**
     * Toggle correct status of a choice
     */
    public Choice toggleCorrectStatus(Long id) {
        logger.debug("Toggling correct status for choice {}", id);

        Choice choice = choiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Choice not found with id: " + id));

        choice.setIsCorrect(!choice.getIsCorrect());  // ✅ FIXED: Use camelCase method

        Choice updated = choiceRepository.save(choice);
        logger.info("Toggled correct status for choice {} to {}", id, updated.getIsCorrect());

        return updated;
    }

    /**
     * Set correct status for a choice
     */
    public Choice setCorrectStatus(Long id, boolean isCorrect) {
        logger.debug("Setting correct status for choice {} to {}", id, isCorrect);

        Choice choice = choiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Choice not found with id: " + id));

        choice.setIsCorrect(isCorrect);  // ✅ FIXED: Use camelCase method

        Choice updated = choiceRepository.save(choice);
        logger.info("Set correct status for choice {} to {}", id, isCorrect);

        return updated;
    }

    /**
     * Count choices for a question
     */
    @Transactional(readOnly = true)
    public long countByQuestion(Long questionId) {
        return choiceRepository.countByQuestionId(questionId);
    }

    /**
     * Count correct choices for a question
     */
    @Transactional(readOnly = true)
    public long countCorrectChoicesByQuestion(Long questionId) {
        return choiceRepository.countByQuestionIdAndIsCorrect(questionId, true);
    }

    /**
     * Validate that a multiple-choice question has at least one correct answer
     */
    @Transactional(readOnly = true)
    public void validateQuestionHasCorrectAnswer(Long questionId) {
        if (!questionHasCorrectAnswers(questionId)) {
            throw new IllegalStateException("Question " + questionId + " must have at least one correct answer");
        }
    }

    /**
     * Delete all choices for a question
     */
    public void deleteAllByQuestion(Long questionId) {
        logger.debug("Deleting all choices for question {}", questionId);
        choiceRepository.deleteByQuestionId(questionId);
        logger.info("Deleted all choices for question {}", questionId);
    }

    /**
     * Create multiple choices at once
     */
    public List<Choice> createMultipleChoices(Long questionId, List<ChoiceData> choicesData) {
        logger.debug("Creating {} choices for question {}", choicesData.size(), questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        Integer startPosition = choiceRepository.findMaxPositionByQuestionId(questionId);
        if (startPosition == null) {
            startPosition = 0;
        }

        List<Choice> choices = new java.util.ArrayList<>();

        for (int i = 0; i < choicesData.size(); i++) {
            ChoiceData data = choicesData.get(i);

            Choice choice = new Choice();
            choice.setQuestion(question);
            choice.setText(data.text);
            choice.setIsCorrect(data.isCorrect);  // ✅ FIXED: Use camelCase method
            choice.setPosition(startPosition + i + 1);

            choices.add(choice);
        }

        List<Choice> saved = choiceRepository.saveAll(choices);
        logger.info("Created {} choices for question {}", saved.size(), questionId);

        return saved;
    }

    /**
     * Helper class for bulk choice creation
     */
    public static class ChoiceData {
        public final String text;
        public final Boolean isCorrect;

        public ChoiceData(String text, Boolean isCorrect) {
            this.text = text;
            this.isCorrect = isCorrect;
        }
    }
}