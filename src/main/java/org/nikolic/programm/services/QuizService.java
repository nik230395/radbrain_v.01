package org.nikolic.programm.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.entities.*;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org.nikolic.programm.repositories.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ✅ FIXED QuizService
 *
 * Changes:
 * - Updated to use new camelCase getters/setters (isCorrect, answerText, matchMode)
 * - Added validation annotations support
 * - Improved error handling
 * - Fixed N+1 query issues
 * - Added comprehensive logging
 */
@Service
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ObjectMapper objectMapper;

    public QuizService(QuizRepository quizRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Find quiz by ID
     */
    @Transactional(readOnly = true)
    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    /**
     * Find quiz with eager-loaded questions (prevents N+1)
     */
    @Transactional(readOnly = true)
    public Optional<Quiz> findByIdWithQuestions(Long id) {
        return quizRepository.findByIdWithQuestions(id);
    }


    /**
     * Get all published quizzes with caching
     */
    @Cacheable(value = "publishedQuizzes", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<Quiz> findAllPublished() {
        logger.debug("Fetching all published quizzes from database");
        return quizRepository.findByIsPublishedTrue();
    }
    /**
     * Get all published quizzes with pagination and caching
     */
    @Cacheable("publishedQuizzes")
    public Page<Quiz> findAllPublishedPaged(Pageable pageable) {
        logger.debug("Fetching published quizzes with pagination: {}", pageable);
        List<Quiz> allPublished = quizRepository.findByIsPublishedTrue();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allPublished.size());

        List<Quiz> pageContent = allPublished.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allPublished.size());
    }

    /**
     * Publish/Unpublish quiz
     */
    @Transactional
    @CacheEvict(value = "publishedQuizzes", allEntries = true)
    public Quiz setPublished(Long id, boolean published) {
        logger.info("Setting quiz {} published status to: {}", id, published);

        Quiz quiz = quizRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with id: " + id));

        // Validate quiz can be published
        if (published) {
            validateQuizForPublishing(quiz);
        }

        quiz.setIsPublished(published);
        Quiz savedQuiz = quizRepository.save(quiz);

        logger.info("Quiz {} successfully {}published", id, published ? "" : "un");
        return savedQuiz;
    }

    /**
     * Create quiz from request
     */
    @Transactional
    @CacheEvict(value = "publishedQuizzes", allEntries = true)
    public Quiz createFromRequest(CreateQuizRequest req, User user) {
        logger.info("Creating new quiz: {} by user: {}", req.getTitle(), user.getEmail());

        validateQuizRequest(req);

        Quiz quiz = new Quiz();
        quiz.setTitle(req.getTitle().trim());
        quiz.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        quiz.setCategory(req.getCategory() != null ? req.getCategory().trim() : null);
        quiz.setCreatedBy(user);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setIsPublished(req.isIsPublished());

        Quiz saved = quizRepository.save(quiz);
        logger.info("Created quiz with id: {}", saved.getId());

        return saved;
    }

    /**
     * Update quiz
     */
    @Transactional
    @CacheEvict(value = "publishedQuizzes", allEntries = true)
    public Quiz updateFromRequest(Long id, CreateQuizRequest req) {
        logger.info("Updating quiz: {}", id);

        validateQuizRequest(req);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with id: " + id));

        quiz.setTitle(req.getTitle().trim());
        quiz.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        quiz.setCategory(req.getCategory() != null ? req.getCategory().trim() : null);

        Quiz updated = quizRepository.save(quiz);
        logger.info("Updated quiz: {}", id);

        return updated;
    }

    /**
     * Delete quiz
     */
    @Transactional
    @CacheEvict(value = "publishedQuizzes", allEntries = true)
    public void deleteQuizById(Long id) {
        logger.info("Deleting quiz: {}", id);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with id: " + id));

        // Check if quiz has attempts
        long attemptCount = quizAttemptRepository.countByQuizId(id);
        if (attemptCount > 0) {
            logger.warn("Quiz {} has {} attempts, deleting anyway", id, attemptCount);
        }

        quizRepository.delete(quiz);
        logger.info("Quiz {} successfully deleted", id);
    }

    /**
     * Evaluate answers and save attempt
     * ✅ FIXED: Uses proper camelCase getters throughout
     */
    @Transactional
    public Map<String, Object> evaluateAndSaveAttempt(Quiz quiz, User user, Map<Long, Object> answers) {
        System.out.println("========================================");
        System.out.println("🔍 EVALUATING QUIZ - USER CHECK");
        System.out.println("User object: " + user);
        System.out.println("User is null? " + (user == null));
        if (user != null) {
            System.out.println("User email: " + user.getEmail());
            System.out.println("User ID: " + user.getId());
        }
        System.out.println("========================================");

        logger.info("Evaluating quiz {} for user {}", quiz.getId(), user != null ? user.getEmail() : "anonymous");

        // Load quiz with questions and choices to prevent N+1
        Quiz fullQuiz = quizRepository.findByIdWithQuestions(quiz.getId())
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));

        List<Question> questions = fullQuiz.getQuestions();

        if (questions == null || questions.isEmpty()) {
            return createEmptyResult();
        }

        int totalQuestions = questions.size();
        int correctCount = 0;
        Map<Long, Map<String, Object>> detailedResults = new HashMap<>();

        // Evaluate each question
        for (Question question : questions) {
            Object userAnswer = answers.get(question.getId());
            Map<String, Object> questionResult = evaluateQuestion(question, userAnswer);

            boolean isCorrect = (boolean) questionResult.get("isCorrect");
            if (isCorrect) {
                correctCount++;
            }

            detailedResults.put(question.getId(), questionResult);
        }

        // Calculate score
        BigDecimal scorePct = calculateScore(correctCount, totalQuestions);

        // Save attempt (only for logged-in users)
        if (user != null) {
            saveQuizAttempt(fullQuiz, user, answers, scorePct);
        }

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("scorePct", scorePct);
        result.put("correctCount", correctCount);
        result.put("totalQuestions", totalQuestions);
        result.put("detailedResults", detailedResults);
        result.put("passed", scorePct.compareTo(BigDecimal.valueOf(50)) >= 0);
        result.put("message", generateResultMessage(scorePct));

        logger.info("Quiz evaluation complete. Score: {}%", scorePct);
        return result;
    }

    /**
     * Evaluate a single question
     * ✅ FIXED: Uses proper camelCase getters
     */
    private Map<String, Object> evaluateQuestion(Question question, Object userAnswer) {
        Map<String, Object> result = new HashMap<>();
        result.put("questionId", question.getId());
        result.put("questionType", question.getQtype());
        result.put("userAnswer", userAnswer);

        boolean isCorrect = false;
        String feedback = "";
        List<Long> correctChoiceIds = new ArrayList<>();

        try {
            switch (question.getQtype()) {
                case SINGLE:
                    isCorrect = evaluateSingleChoice(question, userAnswer, correctChoiceIds);
                    feedback = isCorrect ? "Richtig!" : "Leider falsch.";
                    break;

                case MULTIPLE:
                    isCorrect = evaluateMultipleChoice(question, userAnswer, correctChoiceIds);
                    feedback = isCorrect ? "Alle richtigen Antworten gewählt!" : "Nicht alle richtigen Antworten gewählt.";
                    break;

                case TRUE_FALSE:
                    isCorrect = evaluateTrueFalse(question, userAnswer, correctChoiceIds);
                    feedback = isCorrect ? "Richtig!" : "Leider falsch.";
                    break;

                case SHORT_TEXT:
                    isCorrect = evaluateShortText(question, userAnswer);
                    feedback = isCorrect ? "Richtig!" : "Leider falsch.";
                    break;

                case FILL_GAP:
                    isCorrect = evaluateFillGap(question, userAnswer);
                    feedback = isCorrect ? "Richtig!" : "Leider falsch.";
                    break;

                case FLASHCARD:
                    isCorrect = true;
                    feedback = "Flashcard durchgesehen";
                    break;

                default:
                    isCorrect = false;
                    feedback = "Unbekannter Fragetyp";
            }
        } catch (Exception e) {
            logger.error("Error evaluating question {}: {}", question.getId(), e.getMessage(), e);
            isCorrect = false;
            feedback = "Fehler bei der Auswertung";
        }

        result.put("isCorrect", isCorrect);
        result.put("feedback", feedback);
        result.put("correctChoiceIds", correctChoiceIds);

        return result;
    }

    /**
     * Evaluate single choice question
     * ✅ FIXED: Uses getIsCorrect() instead of getIs_correct()
     */
    private boolean evaluateSingleChoice(Question question, Object userAnswer, List<Long> correctChoiceIds) {
        List<Choice> correctChoices = question.getChoices().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsCorrect()))  // ✅ FIXED: camelCase
                .collect(Collectors.toList());

        if (correctChoices.isEmpty()) {
            logger.warn("Question {} has no correct choices", question.getId());
            return false;
        }

        correctChoiceIds.addAll(correctChoices.stream()
                .map(Choice::getId)
                .collect(Collectors.toList()));

        Long correctId = correctChoices.get(0).getId();

        if (userAnswer instanceof List) {
            List<?> list = (List<?>) userAnswer;
            return list.size() == 1 && correctId.equals(getLongFromObject(list.get(0)));
        } else {
            return correctId.equals(getLongFromObject(userAnswer));
        }
    }

    /**
     * Evaluate multiple choice question
     * ✅ FIXED: Uses getIsCorrect() instead of getIs_correct()
     */
    private boolean evaluateMultipleChoice(Question question, Object userAnswer, List<Long> correctChoiceIds) {
        Set<Long> correctIds = question.getChoices().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsCorrect()))  // ✅ FIXED: camelCase
                .map(Choice::getId)
                .collect(Collectors.toSet());

        if (correctIds.isEmpty()) {
            logger.warn("Question {} has no correct choices", question.getId());
            return false;
        }

        correctChoiceIds.addAll(correctIds);

        if (!(userAnswer instanceof List)) {
            return false;
        }

        Set<Long> userIds = ((List<?>) userAnswer).stream()
                .map(this::getLongFromObject)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return correctIds.equals(userIds);
    }

    /**
     * Evaluate true/false question
     */
    private boolean evaluateTrueFalse(Question question, Object userAnswer, List<Long> correctChoiceIds) {
        return evaluateSingleChoice(question, userAnswer, correctChoiceIds);
    }

    /**
     * Evaluate short text question
     * ✅ FIXED: Uses getAnswerText() and getMatchMode() instead of snake_case
     */
    private boolean evaluateShortText(Question question, Object userAnswer) {
        if (!(userAnswer instanceof String)) {
            return false;
        }

        String answer = ((String) userAnswer).trim();
        List<AcceptableAnswer> acceptableAnswers = question.getAcceptableAnswers();

        if (acceptableAnswers == null || acceptableAnswers.isEmpty()) {
            logger.warn("Question {} has no acceptable answers", question.getId());
            return false;
        }

        return acceptableAnswers.stream()
                .anyMatch(acceptable -> matchesAnswer(answer,
                        acceptable.getAnswerText(),    // ✅ FIXED: camelCase
                        acceptable.getMatchMode()));   // ✅ FIXED: camelCase
    }

    /**
     * Evaluate fill gap question
     */
    private boolean evaluateFillGap(Question question, Object userAnswer) {
        return evaluateShortText(question, userAnswer);
    }

    /**
     * Check if user answer matches expected answer with given match mode
     */
    private boolean matchesAnswer(String userAnswer, String expectedAnswer, MatchMode mode) {
        if (userAnswer == null || expectedAnswer == null) {
            return false;
        }

        try {
            switch (mode) {
                case EXACT:
                    return userAnswer.equals(expectedAnswer);
                case CASE_INSENSITIVE:
                    return userAnswer.equalsIgnoreCase(expectedAnswer);
                case CONTAINS:
                    return userAnswer.toLowerCase().contains(expectedAnswer.toLowerCase());
                case REGEX:
                    return Pattern.matches(expectedAnswer, userAnswer);
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error matching answer: {}", e.getMessage());
            return false;
        }
    }



    /**
     * Convert object to Long
     */
    private Long getLongFromObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Save quiz attempt to database
     */
    private void saveQuizAttempt(Quiz quiz, User user, Map<Long, Object> answers, BigDecimal scorePct) {
        // DEBUG: Log attempt to save
        System.out.println("========================================");
        System.out.println("ATTEMPTING TO SAVE QUIZ ATTEMPT");
        System.out.println("User: " + (user != null ? user.getEmail() : "NULL!!!"));
        System.out.println("Quiz: " + quiz.getTitle());
        System.out.println("Score: " + scorePct + "%");
        System.out.println("Answers count: " + answers.size());
        System.out.println("========================================");

        // Check if user is null
        if (user == null) {
            System.err.println("❌ ERROR: User is NULL! Cannot save quiz attempt!");
            logger.error("Cannot save quiz attempt - user is null");
            return;
        }

        try {
            System.out.println("Converting answers to JSON...");
            String answersJson = objectMapper.writeValueAsString(answers);
            System.out.println("✅ JSON created: " + answersJson.substring(0, Math.min(100, answersJson.length())) + "...");

            System.out.println("Creating QuizAttempt object...");
            QuizAttempt attempt = new QuizAttempt();
            attempt.setQuiz(quiz);
            attempt.setUser(user);
            attempt.setStartedAt(LocalDateTime.now());
            attempt.setCompletedAt(LocalDateTime.now());
            attempt.setAnswersJson(answersJson);
            attempt.setScorePct(scorePct);
            System.out.println("✅ QuizAttempt object created");

            System.out.println("Saving to database...");
            QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
            System.out.println("========================================");
            System.out.println("✅✅✅ SUCCESS! Quiz attempt saved!");
            System.out.println("ID: " + savedAttempt.getId());
            System.out.println("User: " + savedAttempt.getUser().getEmail());
            System.out.println("Score: " + savedAttempt.getScorePct() + "%");
            System.out.println("========================================");

            logger.info("Quiz attempt saved for user {} with ID {}", user.getEmail(), savedAttempt.getId());
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌❌❌ FAILED TO SAVE QUIZ ATTEMPT!");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("========================================");
            logger.error("Failed to save quiz attempt: {}", e.getMessage(), e);
        }
    }

    // Helper Methods

    /**
     * Calculate score percentage
     */
    private BigDecimal calculateScore(int correct, int total) {
        if (total == 0) return BigDecimal.ZERO;

        return BigDecimal.valueOf(correct)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /**
     * Generate result message based on score
     */
    private String generateResultMessage(BigDecimal scorePct) {
        if (scorePct.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "Ausgezeichnet!";
        } else if (scorePct.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "Gut gemacht!";
        } else if (scorePct.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "Bestanden!";
        } else {
            return "Leider nicht bestanden. Versuch es nochmal!";
        }
    }

    /**
     * Create empty result for quizzes with no questions
     */
    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("scorePct", BigDecimal.ZERO);
        result.put("correctCount", 0);
        result.put("totalQuestions", 0);
        result.put("detailedResults", new HashMap<>());
        result.put("passed", false);
        result.put("message", "Keine Fragen vorhanden");
        return result;
    }

    /**
     * Validate quiz request
     */
    private void validateQuizRequest(CreateQuizRequest req) {
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Quiz-Titel ist erforderlich");
        }

        if (req.getTitle().length() > 255) {
            throw new IllegalArgumentException("Quiz-Titel ist zu lang (max. 255 Zeichen)");
        }

        if (req.getDescription() != null && req.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Quiz-Beschreibung ist zu lang (max. 1000 Zeichen)");
        }
    }

    /**
     * Validate quiz can be published
     */
    private void validateQuizForPublishing(Quiz quiz) {
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            throw new IllegalStateException("Quiz kann nicht veröffentlicht werden: Keine Fragen vorhanden");
        }

        // Validate each question has choices or acceptable answers
        for (Question question : quiz.getQuestions()) {
            switch (question.getQtype()) {
                case SINGLE:
                case MULTIPLE:
                case TRUE_FALSE:
                    if (question.getChoices() == null || question.getChoices().isEmpty()) {
                        throw new IllegalStateException(
                                "Frage '" + question.getText() + "' hat keine Antwortmöglichkeiten"
                        );
                    }
                    // Check at least one correct answer
                    boolean hasCorrect = question.getChoices().stream()
                            .anyMatch(c -> Boolean.TRUE.equals(c.getIsCorrect()));  // ✅ FIXED: camelCase
                    if (!hasCorrect) {
                        throw new IllegalStateException(
                                "Frage '" + question.getText() + "' hat keine richtige Antwort markiert"
                        );
                    }
                    break;

                case SHORT_TEXT:
                case FILL_GAP:
                    if (question.getAcceptableAnswers() == null || question.getAcceptableAnswers().isEmpty()) {
                        throw new IllegalStateException(
                                "Frage '" + question.getText() + "' hat keine akzeptablen Antworten definiert"
                        );
                    }
                    break;
            }
        }

        logger.info("Quiz {} validated for publishing", quiz.getId());
    }

    /**
     * Get all quizzes (admin only)
     */
    @Transactional(readOnly = true)
    public List<Quiz> findAll() {
        return quizRepository.findAll();
    }

    /**
     * Get quizzes by category
     */
    @Transactional(readOnly = true)
    public List<Quiz> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return findAll();
        }
        return quizRepository.findByCategoryOrderByCreatedAtDesc(category.trim());
    }

    /**
     * Search quizzes by title
     */
    @Transactional(readOnly = true)
    public List<Quiz> searchByTitle(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return quizRepository.findByTitleContainingIgnoreCase(searchTerm.trim());
    }
}