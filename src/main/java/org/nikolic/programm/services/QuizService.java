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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

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
     * Get all quizzes.
     */
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    /**
     * Evaluate answers and save the quiz attempt for a user.
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

        // Convert answers to JSON string
        try {
            String json = objectMapper.writeValueAsString(answers);
            attempt.setAnswersJson(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing answer JSON", e);
        }

        // Calculate score percentage
        int totalQuestions = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        int correctAnswers = (answers != null) ? answers.size() : 0;

        BigDecimal percentage = BigDecimal.ZERO;
        if (totalQuestions > 0) {
            percentage = BigDecimal.valueOf(correctAnswers)
                    .divide(BigDecimal.valueOf(totalQuestions), 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        attempt.setScorePct(percentage);

        // Save attempt to database
        quizAttemptRepository.save(attempt);

        // Return evaluation results
        Map<String, Object> result = new HashMap<>();
        result.put("quizId", quiz.getId());
        result.put("userEmail", user.getEmail());
        result.put("scorePct", percentage);
        return result;
    }

    /**
     * Create a quiz from a request DTO and user.
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
     * Set published status of a quiz.
     */
    public Quiz setPublished(Long id, boolean published) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + id));
        quiz.setIsPublished(published);
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