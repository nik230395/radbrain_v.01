package org.nikolic.programm.services;

import org.nikolic.programm.entities.AcceptableAnswer;
import org.nikolic.programm.entities.MatchMode;
import org.nikolic.programm.entities.Question;
import org.nikolic.programm.repositories.AcceptableAnswerRepository;
import org.nikolic.programm.repositories.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ FIXED AcceptableAnswerService
 *
 * Changes:
 * - Updated to use new camelCase getters/setters
 * - Added proper error handling
 * - Added validation
 * - Added logging
 */
@Service
@Transactional
public class AcceptableAnswerService {

    private static final Logger logger = LoggerFactory.getLogger(AcceptableAnswerService.class);

    private final AcceptableAnswerRepository acceptableAnswerRepository;
    private final QuestionRepository questionRepository;

    public AcceptableAnswerService(AcceptableAnswerRepository acceptableAnswerRepository,
                                   QuestionRepository questionRepository) {
        this.acceptableAnswerRepository = acceptableAnswerRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Create new acceptable answer
     */
    public AcceptableAnswer createAcceptableAnswer(Long questionId, String answerText, String matchModeStr) {
        logger.debug("Creating acceptable answer for question {}", questionId);

        if (answerText == null || answerText.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer text cannot be empty");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        AcceptableAnswer answer = new AcceptableAnswer();
        answer.setQuestion(question);
        answer.setAnswerText(answerText.trim());  // ✅ FIXED: Use camelCase method

        // Parse match mode with validation
        MatchMode matchMode = parseMatchMode(matchModeStr);
        answer.setMatchMode(matchMode);  // ✅ FIXED: Use camelCase method

        AcceptableAnswer saved = acceptableAnswerRepository.save(answer);
        logger.info("Created acceptable answer {} for question {}", saved.getId(), questionId);

        return saved;
    }

    /**
     * Get all acceptable answers for a question
     */
    @Transactional(readOnly = true)
    public List<AcceptableAnswer> getAnswersByQuestion(Long questionId) {
        logger.debug("Fetching acceptable answers for question {}", questionId);
        return acceptableAnswerRepository.findByQuestionId(questionId);
    }

    /**
     * Update acceptable answer
     */
    public AcceptableAnswer updateAcceptableAnswer(Long id, String answerText, String matchModeStr) {
        logger.debug("Updating acceptable answer {}", id);

        AcceptableAnswer answer = acceptableAnswerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Acceptable answer not found with id: " + id));

        if (answerText != null && !answerText.trim().isEmpty()) {
            answer.setAnswerText(answerText.trim());  // ✅ FIXED: Use camelCase method
        }

        if (matchModeStr != null && !matchModeStr.trim().isEmpty()) {
            MatchMode matchMode = parseMatchMode(matchModeStr);
            answer.setMatchMode(matchMode);  // ✅ FIXED: Use camelCase method
        }

        AcceptableAnswer updated = acceptableAnswerRepository.save(answer);
        logger.info("Updated acceptable answer {}", id);

        return updated;
    }

    /**
     * Delete acceptable answer
     */
    public void deleteAcceptableAnswer(Long id) {
        logger.debug("Deleting acceptable answer {}", id);

        if (!acceptableAnswerRepository.existsById(id)) {
            throw new IllegalArgumentException("Acceptable answer not found with id: " + id);
        }

        acceptableAnswerRepository.deleteById(id);
        logger.info("Deleted acceptable answer {}", id);
    }

    /**
     * Get acceptable answers by match mode
     */
    @Transactional(readOnly = true)
    public List<AcceptableAnswer> getAnswersByQuestionAndMatchMode(Long questionId, MatchMode matchMode) {
        logger.debug("Fetching acceptable answers for question {} with match mode {}", questionId, matchMode);
        return acceptableAnswerRepository.findByQuestionIdAndMatchMode(questionId, matchMode);
    }

    /**
     * Check if answer text matches any acceptable answer for a question
     */
    @Transactional(readOnly = true)
    public boolean isAnswerAcceptable(Long questionId, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        List<AcceptableAnswer> acceptableAnswers = getAnswersByQuestion(questionId);

        if (acceptableAnswers.isEmpty()) {
            logger.warn("Question {} has no acceptable answers defined", questionId);
            return false;
        }

        boolean matches = acceptableAnswers.stream()
                .anyMatch(acceptable -> matchesAnswer(userAnswer, acceptable));

        logger.debug("User answer '{}' for question {} matches: {}", userAnswer, questionId, matches);

        return matches;
    }

    /**
     * Bulk create acceptable answers for a question
     */
    public List<AcceptableAnswer> createMultipleAcceptableAnswers(Long questionId,
                                                                  List<String> answerTexts,
                                                                  MatchMode matchMode) {
        logger.debug("Creating {} acceptable answers for question {}", answerTexts.size(), questionId);

        if (answerTexts == null || answerTexts.isEmpty()) {
            throw new IllegalArgumentException("Answer texts list cannot be empty");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        List<AcceptableAnswer> answers = answerTexts.stream()
                .filter(text -> text != null && !text.trim().isEmpty())
                .map(text -> {
                    AcceptableAnswer answer = new AcceptableAnswer();
                    answer.setQuestion(question);
                    answer.setAnswerText(text.trim());  // ✅ FIXED: Use camelCase method
                    answer.setMatchMode(matchMode);     // ✅ FIXED: Use camelCase method
                    return answer;
                })
                .collect(Collectors.toList());

        List<AcceptableAnswer> saved = acceptableAnswerRepository.saveAll(answers);
        logger.info("Created {} acceptable answers for question {}", saved.size(), questionId);

        return saved;
    }

    // Private Helper Methods

    /**
     * Check if user answer matches acceptable answer
     */
    private boolean matchesAnswer(String userAnswer, AcceptableAnswer acceptable) {
        String acceptableText = acceptable.getAnswerText();  // ✅ FIXED: Use camelCase method
        MatchMode mode = acceptable.getMatchMode();          // ✅ FIXED: Use camelCase method

        if (acceptableText == null || userAnswer == null) {
            return false;
        }

        try {
            switch (mode) {
                case EXACT:
                    return userAnswer.equals(acceptableText);

                case CASE_INSENSITIVE:
                    return userAnswer.equalsIgnoreCase(acceptableText);

                case CONTAINS:
                    return userAnswer.toLowerCase().contains(acceptableText.toLowerCase());

                case REGEX:
                    return userAnswer.matches(acceptableText);

                default:
                    logger.warn("Unknown match mode: {}", mode);
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error matching answer with mode {}: {}", mode, e.getMessage());
            return false;
        }
    }

    /**
     * Parse match mode string with validation
     */
    private MatchMode parseMatchMode(String matchModeStr) {
        if (matchModeStr == null || matchModeStr.trim().isEmpty()) {
            return MatchMode.EXACT; // Default
        }

        try {
            String normalized = matchModeStr.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");
            return MatchMode.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid match mode '{}', using EXACT as default", matchModeStr);
            return MatchMode.EXACT;
        }
    }

    /**
     * Count acceptable answers for a question
     */
    @Transactional(readOnly = true)
    public long countByQuestion(Long questionId) {
        return acceptableAnswerRepository.countByQuestionId(questionId);
    }

    /**
     * Delete all acceptable answers for a question
     */
    public void deleteAllByQuestion(Long questionId) {
        logger.debug("Deleting all acceptable answers for question {}", questionId);
        acceptableAnswerRepository.deleteByQuestionId(questionId);
        logger.info("Deleted all acceptable answers for question {}", questionId);
    }
}