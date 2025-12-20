package org.nikolic.programm.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.entities.*;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org.nikolic.programm.repositories.QuizRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Transactional
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ObjectMapper objectMapper;

    public QuizService(QuizRepository quizRepository,
                       UserRepository userRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a quiz from a request DTO and user
     */
    public Quiz createFromRequest(CreateQuizRequest req, User user) {
        Quiz quiz = new Quiz();
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        quiz.setCreatedBy(user);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setIsPublished(false);
        return quizRepository.save(quiz);
    }

    /**
     * Update a quiz from a request DTO
     */
    public Quiz updateFromRequest(Long id, CreateQuizRequest req) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + id));
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        return quizRepository.save(quiz);
    }

    /**
     * Set publish status of a quiz
     */
    public Quiz setPublished(Long id, boolean published) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + id));
        quiz.setIsPublished(published);
        return quizRepository.save(quiz);
    }

    /**
     * Retrieve all quizzes
     */
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    /**
     * Delete a quiz by ID
     */
    public void deleteQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Quiz not found with ID: " + id));
        quizRepository.delete(quiz);
    }

    /**
     * Find a quiz by ID
     */
    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    /**
     * Get all published quizzes
     */
    public List<Quiz> findPublished() {
        return quizRepository.findByIsPublishedTrue();
    }

    /**
     * Evaluate quiz answers and save attempt
     * @param quiz The quiz being attempted
     * @param user The user taking the quiz (can be null for anonymous)
     * @param answers Map of questionId to answer (can be List<Long> for choice IDs or String for text)
     * @return Map with results including score, correct/total, and attemptId if user is authenticated
     */
    public Map<String, Object> evaluateAndSaveAttempt(Quiz quiz, User user, Map<Long, Object> answers) {
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("score", 0);
            result.put("correct", 0);
            result.put("total", 0);
            result.put("message", "Quiz has no questions");
            return result;
        }

        int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = 0;

        // Evaluate each question
        for (Question question : quiz.getQuestions()) {
            if (question.getId() == null) continue;
            
            Object userAnswer = answers.get(question.getId());
            if (userAnswer == null) continue; // No answer provided for this question

            boolean isCorrect = evaluateQuestion(question, userAnswer);
            if (isCorrect) {
                correctAnswers++;
            }
        }

        // Calculate score percentage
        BigDecimal scorePct = totalQuestions > 0 
            ? BigDecimal.valueOf(correctAnswers)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalQuestions), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // Save attempt if user is authenticated
        Long attemptId = null;
        if (user != null) {
            try {
                QuizAttempt attempt = new QuizAttempt();
                attempt.setQuiz(quiz);
                attempt.setUser(user);
                attempt.setStartedAt(LocalDateTime.now());
                attempt.setCompletedAt(LocalDateTime.now());
                attempt.setScorePct(scorePct);
                // Convert answers to JSON
                attempt.setAnswersJson(objectMapper.writeValueAsString(answers));
                QuizAttempt saved = quizAttemptRepository.save(attempt);
                attemptId = saved.getId();
            } catch (Exception e) {
                // Log but don't fail the evaluation
                logger.error("Failed to save quiz attempt for user ID {}: {}", user.getId(), e.getMessage(), e);
            }
        }

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("score", scorePct.doubleValue());
        result.put("correct", correctAnswers);
        result.put("total", totalQuestions);
        if (attemptId != null) {
            result.put("attemptId", attemptId);
        }
        return result;
    }

    /**
     * Evaluate a single question's answer
     */
    private boolean evaluateQuestion(Question question, Object userAnswer) {
        if (question.getQtype() == null) return false;

        switch (question.getQtype()) {
            case SINGLE:
                return evaluateSingleChoice(question, userAnswer);
            case MULTIPLE:
                return evaluateMultipleChoice(question, userAnswer);
            case TRUE_FALSE:
                return evaluateTrueFalse(question, userAnswer);
            case SHORT_TEXT:
            case FILL_GAP:
                return evaluateTextAnswer(question, userAnswer);
            case FLASHCARD:
                // Flashcards are typically self-evaluated or informational
                return true;
            default:
                return false;
        }
    }

    /**
     * Evaluate single choice question
     */
    private boolean evaluateSingleChoice(Question question, Object userAnswer) {
        if (question.getChoices() == null || question.getChoices().isEmpty()) return false;
        
        Long selectedChoiceId = parseChoiceId(userAnswer);
        if (selectedChoiceId == null) return false;

        return question.getChoices().stream()
            .filter(c -> c.getId() != null && c.getId().equals(selectedChoiceId))
            .findFirst()
            .map(c -> Boolean.TRUE.equals(c.getIsCorrect()))
            .orElse(false);
    }

    /**
     * Evaluate multiple choice question
     */
    private boolean evaluateMultipleChoice(Question question, Object userAnswer) {
        if (question.getChoices() == null || question.getChoices().isEmpty()) return false;

        Set<Long> selectedIds = parseChoiceIds(userAnswer);
        if (selectedIds.isEmpty()) return false;

        // Get all correct choice IDs
        Set<Long> correctIds = new HashSet<>();
        for (Choice c : question.getChoices()) {
            if (Boolean.TRUE.equals(c.getIsCorrect()) && c.getId() != null) {
                correctIds.add(c.getId());
            }
        }

        // Must match exactly
        return selectedIds.equals(correctIds);
    }

    /**
     * Evaluate true/false question
     */
    private boolean evaluateTrueFalse(Question question, Object userAnswer) {
        if (question.getChoices() == null || question.getChoices().isEmpty()) return false;
        
        Long selectedChoiceId = parseChoiceId(userAnswer);
        if (selectedChoiceId == null) return false;

        return question.getChoices().stream()
            .filter(c -> c.getId() != null && c.getId().equals(selectedChoiceId))
            .findFirst()
            .map(c -> Boolean.TRUE.equals(c.getIsCorrect()))
            .orElse(false);
    }

    /**
     * Evaluate text answer (short text or fill in the gap)
     */
    private boolean evaluateTextAnswer(Question question, Object userAnswer) {
        if (question.getAcceptableAnswers() == null || question.getAcceptableAnswers().isEmpty()) {
            return false;
        }

        String answer = userAnswer != null ? userAnswer.toString().trim() : "";
        if (answer.isEmpty()) return false;

        // Check against all acceptable answers
        for (AcceptableAnswer acceptable : question.getAcceptableAnswers()) {
            if (matchesAnswer(answer, acceptable.getAnswerText(), acceptable.getMatchMode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user answer matches acceptable answer based on match mode
     */
    private boolean matchesAnswer(String userAnswer, String acceptableAnswer, MatchMode mode) {
        if (userAnswer == null || acceptableAnswer == null) return false;
        if (mode == null) mode = MatchMode.EXACT;

        switch (mode) {
            case EXACT:
                return userAnswer.equals(acceptableAnswer);
            case CASE_INSENSITIVE:
                return userAnswer.equalsIgnoreCase(acceptableAnswer);
            case CONTAINS:
                return userAnswer.toLowerCase().contains(acceptableAnswer.toLowerCase());
            case REGEX:
                try {
                    // Limit regex matching to prevent ReDoS attacks
                    // Only allow simple patterns and set length limits
                    if (acceptableAnswer.length() > 100) {
                        logger.warn("Regex pattern too long, treating as non-match");
                        return false;
                    }
                    if (userAnswer.length() > 10000) {
                        logger.warn("User answer too long for regex matching, treating as non-match");
                        return false;
                    }
                    Pattern pattern = Pattern.compile(acceptableAnswer);
                    return pattern.matcher(userAnswer).matches();
                } catch (Exception e) {
                    logger.warn("Invalid regex pattern or matching error: {}", e.getMessage());
                    return false;
                }
            default:
                return false;
        }
    }

    /**
     * Parse a single choice ID from user answer
     */
    private Long parseChoiceId(Object answer) {
        if (answer == null) return null;
        
        try {
            if (answer instanceof Number) {
                return ((Number) answer).longValue();
            }
            if (answer instanceof String) {
                return Long.parseLong((String) answer);
            }
            if (answer instanceof List && !((List<?>) answer).isEmpty()) {
                Object first = ((List<?>) answer).get(0);
                if (first instanceof Number) {
                    return ((Number) first).longValue();
                }
                if (first instanceof String) {
                    return Long.parseLong((String) first);
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /**
     * Parse multiple choice IDs from user answer
     */
    private Set<Long> parseChoiceIds(Object answer) {
        Set<Long> ids = new HashSet<>();
        if (answer == null) return ids;

        try {
            if (answer instanceof List) {
                for (Object item : (List<?>) answer) {
                    if (item instanceof Number) {
                        ids.add(((Number) item).longValue());
                    } else if (item instanceof String) {
                        ids.add(Long.parseLong((String) item));
                    }
                }
            } else if (answer instanceof Number) {
                ids.add(((Number) answer).longValue());
            } else if (answer instanceof String) {
                ids.add(Long.parseLong((String) answer));
            }
        } catch (NumberFormatException e) {
            return new HashSet<>();
        }
        return ids;
    }
}