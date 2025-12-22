package org.nikolic.programm. services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson. databind.ObjectMapper;
import org.nikolic.programm. dtos.CreateQuizRequest;
import org.nikolic. programm.entities.*;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org. nikolic.programm.repositories. QuizRepository;
import org. springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java. util.*;

@Service
public class QuizService {
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

    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    // ✅ Fixed: Use the correct repository method
    public List<Quiz> getPublishedQuizzes() {
        return quizRepository.findByIsPublishedTrueOrderByCreatedAtDesc();
    }

    /**
     * Evaluate answers properly and save quiz attempt
     */
    public Map<String, Object> evaluateAndSaveAttempt(Quiz quiz, User user, Map<Long, Object> answers) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for a Quiz Attempt");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setCompletedAt(LocalDateTime.now());

        // Convert answers to JSON
        try {
            String json = objectMapper.writeValueAsString(answers);
            attempt.setAnswersJson(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing answer JSON", e);
        }

        // Calculate REAL score by validating answers
        List<Question> questions = quiz.getQuestions();
        if (questions == null || questions.isEmpty()) {
            attempt.setScorePct(BigDecimal.ZERO);
            quizAttemptRepository.save(attempt);
            return buildResult(quiz, user, BigDecimal.ZERO, 0, 0);
        }

        int totalQuestions = questions.size();
        int correctAnswers = 0;

        // Evaluate each question
        for (Question question :  questions) {
            Object userAnswer = answers.get(question. getId());
            if (userAnswer == null) {
                continue; // Unanswered question
            }

            boolean isCorrect = evaluateQuestion(question, userAnswer);
            if (isCorrect) {
                correctAnswers++;
            }
        }

        // Calculate percentage
        BigDecimal percentage = BigDecimal. valueOf(correctAnswers)
                .divide(BigDecimal.valueOf(totalQuestions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        attempt.setScorePct(percentage);
        quizAttemptRepository.save(attempt);

        return buildResult(quiz, user, percentage, correctAnswers, totalQuestions);
    }

    /**
     * Evaluate a single question
     */
    private boolean evaluateQuestion(Question question, Object userAnswer) {
        switch (question.getQtype()) {
            case SINGLE:
                return evaluateSingleChoice(question, userAnswer);

            case MULTIPLE:
                return evaluateMultipleChoice(question, userAnswer);

            case SHORT_TEXT:
            case FILL_GAP:
                return evaluateTextAnswer(question, userAnswer);

            case TRUE_FALSE:
                return evaluateTrueFalse(question, userAnswer);

            default:
                return false; // Unknown question type
        }
    }

    /**
     * Evaluate SINGLE choice question (one correct answer)
     */
    private boolean evaluateSingleChoice(Question question, Object userAnswer) {
        if (question.getChoices() == null || question.getChoices().isEmpty()) {
            return false;
        }

        Long selectedChoiceId;
        if (userAnswer instanceof List) {
            List<? > list = (List<?>) userAnswer;
            if (list.isEmpty()) return false;
            selectedChoiceId = convertToLong(list.get(0));
        } else {
            selectedChoiceId = convertToLong(userAnswer);
        }

        if (selectedChoiceId == null) return false;

        // Find the selected choice
        for (Choice choice : question.getChoices()) {
            if (choice.getId().equals(selectedChoiceId)) {
                return Boolean.TRUE. equals(choice.getIs_correct()); // ✅ Fixed: Use getIs_correct()
            }
        }
        return false;
    }

    /**
     * Evaluate MULTIPLE choice question (multiple correct answers)
     */
    private boolean evaluateMultipleChoice(Question question, Object userAnswer) {
        if (question.getChoices() == null || question.getChoices().isEmpty()) {
            return false;
        }

        List<?> selectedIds;
        if (userAnswer instanceof List) {
            selectedIds = (List<?>) userAnswer;
        } else {
            selectedIds = List.of(userAnswer);
        }

        Set<Long> userSelection = new HashSet<>();
        for (Object id : selectedIds) {
            Long choiceId = convertToLong(id);
            if (choiceId != null) {
                userSelection.add(choiceId);
            }
        }

        // Get all correct choice IDs
        Set<Long> correctIds = new HashSet<>();
        for (Choice choice : question.getChoices()) {
            if (Boolean.TRUE.equals(choice.getIs_correct())) { // ✅ Fixed: Use getIs_correct()
                correctIds.add(choice.getId());
            }
        }

        // User must select EXACTLY the correct choices (no more, no less)
        return userSelection.equals(correctIds);
    }

    /**
     * Evaluate text answer (SHORT_TEXT, FILL_GAP)
     */
    private boolean evaluateTextAnswer(Question question, Object userAnswer) {
        if (!(userAnswer instanceof String)) {
            return false;
        }

        String answer = ((String) userAnswer).trim();
        if (answer.isEmpty()) {
            return false;
        }

        // Check against acceptable answers
        if (question.getAcceptableAnswers() == null || question.getAcceptableAnswers().isEmpty()) {
            return false;
        }

        for (AcceptableAnswer acceptable : question.getAcceptableAnswers()) {
            if (matchesAcceptableAnswer(answer, acceptable)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluate TRUE/FALSE question
     */
    private boolean evaluateTrueFalse(Question question, Object userAnswer) {
        return evaluateSingleChoice(question, userAnswer);
    }

    /**
     * Check if user answer matches an acceptable answer
     */
    private boolean matchesAcceptableAnswer(String userAnswer, AcceptableAnswer acceptable) {
        String acceptableText = acceptable.getAnswer_text();
        if (acceptableText == null) return false;

        switch (acceptable.getMatch_mode()) {
            case EXACT:
                return userAnswer.equals(acceptableText);

            case CASE_INSENSITIVE:
                return userAnswer.equalsIgnoreCase(acceptableText);

            case CONTAINS:
                return userAnswer.toLowerCase().contains(acceptableText.toLowerCase());

            case REGEX:
                try {
                    return userAnswer. matches(acceptableText);
                } catch (Exception e) {
                    return false; // Invalid regex
                }

            default:
                return false;
        }
    }

    /**
     * Helper to convert various number types to Long
     */
    private Long convertToLong(Object obj) {
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
     * Build evaluation result map
     */
    private Map<String, Object> buildResult(Quiz quiz, User user, BigDecimal percentage,
                                            int correct, int total) {
        Map<String, Object> result = new HashMap<>();
        result.put("quizId", quiz.getId());
        result.put("quizTitle", quiz.getTitle());
        result.put("userEmail", user.getEmail());
        result.put("scorePct", percentage);
        result.put("correctAnswers", correct);
        result.put("totalQuestions", total);
        return result;
    }

    // === CRUD Operations ===

    public Quiz createFromRequest(CreateQuizRequest req, User user) {
        Quiz quiz = new Quiz();
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        quiz.setCreatedBy(user);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setIsPublished(false);
        return quizRepository.save(quiz);
    }

    public Quiz updateFromRequest(Long id, CreateQuizRequest req) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + id));
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        return quizRepository.save(quiz);
    }

    public Quiz setPublished(Long id, boolean published) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + id));
        quiz.setIsPublished(published);
        return quizRepository.save(quiz);
    }

    public void deleteQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Quiz not found with ID:  " + id));
        quizRepository.delete(quiz);
    }
}