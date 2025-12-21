package org.nikolic.programm.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.QuizAttempt;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org.nikolic.programm.repositories.QuizRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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

    /**
     * Find a quiz by its ID.
     */
    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    /**
     * Evaluate answers and save the quiz attempt for a user.
     */
    public Map<String, Object> evaluateAndSaveAttempt(Quiz quiz, User user, Map<Long, Object> answers) {
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz cannot be null.");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for a Quiz Attempt.");
        }

        // Create and populate QuizAttempt object
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setCompletedAt(LocalDateTime.now()); // Assuming instant submission

        // Serialize answers to JSON
        try {
            String jsonAnswers = objectMapper.writeValueAsString(answers != null ? answers : Map.of());
            attempt.setAnswersJson(jsonAnswers);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing answer JSON.", e);
        }

        // Calculate score (assuming percentage)
        int totalQuestions = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        int correctAnswers = answers != null ? evaluateCorrectAnswers(answers) : 0;

        BigDecimal percentage = BigDecimal.ZERO;
        if (totalQuestions > 0) {
            percentage = BigDecimal.valueOf(correctAnswers)
                    .divide(BigDecimal.valueOf(totalQuestions), 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        attempt.setScorePct(percentage);

        // Save attempt and return evaluation results
        quizAttemptRepository.save(attempt);

        Map<String, Object> result = new HashMap<>();
        result.put("quizId", quiz.getId());
        result.put("userEmail", user.getEmail());
        result.put("scorePct", percentage);

        return result;
    }

    /**
     * Helper method to evaluate the number of correct answers.
     * Replace this with actual logic to validate answers with quiz questions.
     */
    private int evaluateCorrectAnswers(Map<Long, Object> answers) {
        // Placeholder logic for demonstration purposes
        return (int) answers.entrySet().stream().filter(entry -> validateAnswer(entry)).count();
    }

    private boolean validateAnswer(Map.Entry<Long, Object> entry) {
        // Use actual validation criteria for quiz answers here
        return true;
    }

    /**
     * Create a quiz from a request DTO and user.
     */
    public Quiz createFromRequest(CreateQuizRequest req, User user) {
        if (req == null || user == null) {
            throw new IllegalArgumentException("Request or user cannot be null.");
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        quiz.setCreatedBy(user);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setIsPublished(false);

        return quizRepository.save(quiz);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    public Quiz setPublished(Long id, boolean isPublished) {
        // Find the quiz by ID
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + id));

        // Update the publication status
        quiz.setIsPublished(isPublished);

        // Save the updated quiz back to the database
        return quizRepository.save(quiz);
    }

    /**
     * Update a quiz based on the request data.
     */
    public Quiz updateFromRequest(Long id, CreateQuizRequest req) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + id));
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        return quizRepository.save(quiz);
    }

    /**
     * Delete a quiz by ID.
     */
    public void deleteQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Quiz not found with ID: " + id));
        quizRepository.delete(quiz);
    }
}