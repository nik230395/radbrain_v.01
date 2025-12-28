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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * QuizService - Verbessert mit allen Fixes
 *
 * Änderungen:
 * - setPublished() Methode hinzugefügt (KRITISCHER FIX!)
 * - N+1 Query Problem behoben
 * - Caching implementiert
 * - Besseres Error Handling
 * - Performance-Optimierungen
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
     * Findet Quiz by ID
     */
    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    /**
     * Findet Quiz mit eager-loaded Questions (verhindert N+1)
     */
    public Optional<Quiz> findByIdWithQuestions(Long id) {
        return quizRepository.findByIdWithQuestions(id);
    }

    /**
     * Alle veröffentlichten Quizzes mit Caching
     */
    @Cacheable(value = "publishedQuizzes", unless = "#result.isEmpty()")
    public List<Quiz> findAllPublished() {
        logger.debug("Fetching all published quizzes from database");
        return quizRepository.findByIsPublishedTrue();
    }

    /**
     * KRITISCHER FIX: Publish/Unpublish Methode
     * Diese Methode fehlte und wurde in AdminQuizController aufgerufen!
     */
    @Transactional
    @CacheEvict(value = "publishedQuizzes", allEntries = true)
    public Quiz setPublished(Long id, boolean published) {
        logger.info("Setting quiz {} published status to: {}", id, published);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz nicht gefunden mit ID: " + id));

        // Validierung: Quiz muss Fragen haben
        if (published && (quiz.getQuestions() == null || quiz.getQuestions().isEmpty())) {
            throw new IllegalStateException("Quiz kann nicht veröffentlicht werden: Keine Fragen vorhanden");
        }

        quiz.setIsPublished(published);
        Quiz savedQuiz = quizRepository.save(quiz);

        logger.info("Quiz {} successfully {}published", id, published ? "" : "un");
        return savedQuiz;
    }

    /**
     * Erstellt Quiz aus Request
     */
    @Transactional
    @CacheEvict(value = "publishedQuizzes", allEntries = true)
    public Quiz createFromRequest(CreateQuizRequest req, User user) {
        logger.info("Creating new quiz: {} by user: {}", req.getTitle(), user.getEmail());

        validateQuizRequest(req);

        Quiz quiz = new Quiz();
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        quiz.setCreatedBy(user);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setIsPublished(false); // Standardmäßig nicht veröffentlicht

        return quizRepository.save(quiz);
    }

    /**
     * Aktualisiert Quiz
     */
    @Transactional
    @CacheEvict(value = "publishedQuizzes", allEntries = true)
    public Quiz updateFromRequest(Long id, CreateQuizRequest req) {
        logger.info("Updating quiz: {}", id);

        validateQuizRequest(req);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz nicht gefunden mit ID: " + id));

        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());

        return quizRepository.save(quiz);
    }

    /**
     * Löscht Quiz
     */
    @Transactional
    @CacheEvict(value = "publishedQuizzes", allEntries = true)
    public void deleteQuizById(Long id) {
        logger.info("Deleting quiz: {}", id);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz nicht gefunden mit ID: " + id));

        // Prüfe ob Quiz Attempts hat
        long attemptCount = quizAttemptRepository.countByQuizId(id);
        if (attemptCount > 0) {
            logger.warn("Quiz {} has {} attempts, deleting anyway", id, attemptCount);
        }

        quizRepository.delete(quiz);
        logger.info("Quiz {} successfully deleted", id);
    }

    /**
     * HAUPTMETHODE: Evaluiert Antworten und speichert Versuch
     * Optimiert gegen N+1 Problem
     */
    @Transactional
    public Map<String, Object> evaluateAndSaveAttempt(Quiz quiz, User user, Map<Long, Object> answers) {
        logger.info("Evaluating quiz {} for user {}", quiz.getId(), user != null ? user.getEmail() : "anonymous");

        Map<String, Object> result = new HashMap<>();

        // Verwende eager-loaded Questions um N+1 zu vermeiden
        Quiz fullQuiz = quizRepository.findByIdWithQuestions(quiz.getId())
                .orElseThrow(() -> new NoSuchElementException("Quiz nicht gefunden"));

        List<Question> questions = fullQuiz.getQuestions();

        if (questions == null || questions.isEmpty()) {
            return createEmptyResult();
        }

        int totalQuestions = questions.size();
        int correctCount = 0;
        Map<Long, Map<String, Object>> detailedResults = new HashMap<>();

        // Jede Frage auswerten
        for (Question question : questions) {
            Object userAnswer = answers.get(question.getId());
            Map<String, Object> questionResult = evaluateQuestion(question, userAnswer);

            boolean isCorrect = (boolean) questionResult.get("isCorrect");
            if (isCorrect) {
                correctCount++;
            }

            detailedResults.put(question.getId(), questionResult);
        }

        // Score berechnen
        BigDecimal scorePct = calculateScore(correctCount, totalQuestions);

        // Versuch speichern (nur für eingeloggte User)
        if (user != null) {
            saveQuizAttempt(fullQuiz, user, answers, scorePct);
        }

        // Result zusammenstellen
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
     * Evaluiert eine einzelne Frage
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
            logger.error("Error evaluating question {}: {}", question.getId(), e.getMessage());
            isCorrect = false;
            feedback = "Fehler bei der Auswertung";
        }

        result.put("isCorrect", isCorrect);
        result.put("feedback", feedback);
        result.put("correctChoiceIds", correctChoiceIds);

        return result;
    }

    // Einzelne Evaluierungsmethoden (unverändert, aber mit besserer Fehlerbehandlung)

    private boolean evaluateSingleChoice(Question question, Object userAnswer, List<Long> correctChoiceIds) {
        List<Choice> correctChoices = question.getChoices().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIs_correct()))
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

    private boolean evaluateMultipleChoice(Question question, Object userAnswer, List<Long> correctChoiceIds) {
        Set<Long> correctIds = question.getChoices().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIs_correct()))
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

    private boolean evaluateTrueFalse(Question question, Object userAnswer, List<Long> correctChoiceIds) {
        return evaluateSingleChoice(question, userAnswer, correctChoiceIds);
    }

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
                .anyMatch(acceptable -> matchesAnswer(answer, acceptable.getAnswer_text(), acceptable.getMatch_mode()));
    }

    private boolean evaluateFillGap(Question question, Object userAnswer) {
        return evaluateShortText(question, userAnswer);
    }

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

    private void saveQuizAttempt(Quiz quiz, User user, Map<Long, Object> answers, BigDecimal scorePct) {
        try {
            String answersJson = objectMapper.writeValueAsString(answers);

            QuizAttempt attempt = new QuizAttempt();
            attempt.setQuiz(quiz);
            attempt.setUser(user);
            attempt.setStartedAt(LocalDateTime.now());
            attempt.setCompletedAt(LocalDateTime.now());
            attempt.setAnswersJson(answersJson);
            attempt.setScorePct(scorePct);

            quizAttemptRepository.save(attempt);
            logger.info("Quiz attempt saved for user {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to save quiz attempt: {}", e.getMessage(), e);
        }
    }

    // Helper Methods

    private BigDecimal calculateScore(int correct, int total) {
        if (total == 0) return BigDecimal.ZERO;

        return BigDecimal.valueOf(correct)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

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

    private void validateQuizRequest(CreateQuizRequest req) {
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Quiz-Titel ist erforderlich");
        }

        if (req.getTitle().length() > 255) {
            throw new IllegalArgumentException("Quiz-Titel ist zu lang (max. 255 Zeichen)");
        }
    }
}