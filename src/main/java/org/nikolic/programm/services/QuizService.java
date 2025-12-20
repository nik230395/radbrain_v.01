package org.nikolic.programm.services;

import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.exceptions.QuizAlreadyExistsException;
import org.nikolic.programm.exceptions.QuizNotFoundException;
import org.nikolic.programm.repositories.QuizRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    public QuizService(QuizRepository quizRepository,
                       UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a quiz from a request DTO and user
     */
    public Quiz createFromRequest(CreateQuizRequest req, User user) {
        logger.info("Creating quiz with title '{}' for user '{}'", req.getTitle(), user.getEmail());
        
        // Validate input
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            logger.error("Quiz creation failed: title is empty");
            throw new IllegalArgumentException("Quiz title cannot be empty");
        }
        
        // Check if quiz with same title already exists for this user
        List<Quiz> existingQuizzes = quizRepository.findAll();
        boolean exists = existingQuizzes.stream()
                .anyMatch(q -> q.getTitle().equalsIgnoreCase(req.getTitle().trim()) 
                        && q.getCreatedBy() != null 
                        && q.getCreatedBy().getId().equals(user.getId()));
        
        if (exists) {
            logger.error("Quiz creation failed: quiz with title '{}' already exists for user '{}'", 
                    req.getTitle(), user.getEmail());
            throw new QuizAlreadyExistsException(
                    String.format("A quiz with title '%s' already exists", req.getTitle()));
        }
        
        Quiz quiz = new Quiz();
        quiz.setTitle(req.getTitle().trim());
        quiz.setDescription(req.getDescription());
        quiz.setCreatedBy(user);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setIsPublished(false);
        
        Quiz saved = quizRepository.save(quiz);
        logger.info("Successfully created quiz with ID {} and title '{}'", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * Update a quiz from a request DTO
     */
    public Quiz updateFromRequest(Long id, CreateQuizRequest req) {
        logger.info("Updating quiz with ID {}", id);
        
        // Validate input
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            logger.error("Quiz update failed: title is empty for quiz ID {}", id);
            throw new IllegalArgumentException("Quiz title cannot be empty");
        }
        
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Quiz update failed: quiz not found with ID {}", id);
                    return new QuizNotFoundException("Quiz not found with ID: " + id);
                });
        
        quiz.setTitle(req.getTitle().trim());
        quiz.setDescription(req.getDescription());
        
        Quiz updated = quizRepository.save(quiz);
        logger.info("Successfully updated quiz with ID {} and title '{}'", updated.getId(), updated.getTitle());
        return updated;
    }

    /**
     * Set publish status of a quiz
     */
    public Quiz setPublished(Long id, boolean published) {
        logger.info("Setting published status to {} for quiz ID {}", published, id);
        
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Failed to set published status: quiz not found with ID {}", id);
                    return new QuizNotFoundException("Quiz not found with ID: " + id);
                });
        
        quiz.setIsPublished(published);
        Quiz updated = quizRepository.save(quiz);
        logger.info("Successfully set published status to {} for quiz ID {}", published, id);
        return updated;
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
        logger.info("Attempting to delete quiz with ID {}", id);
        
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Quiz deletion failed: quiz not found with ID {}", id);
                    return new QuizNotFoundException("Quiz not found with ID: " + id);
                });
        
        quizRepository.delete(quiz);
        logger.info("Successfully deleted quiz with ID {} and title '{}'", id, quiz.getTitle());
    }

    /**
     * Find a quiz by ID (wrapper for repository method)
     */
    public java.util.Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    /**
     * Evaluate quiz attempt and save results
     * This is a placeholder method that needs to be implemented properly
     * based on business requirements
     */
    public Map<String, Object> evaluateAndSaveAttempt(Quiz quiz, User user, Map<Long, Object> answers) {
        // TODO: Implement proper quiz evaluation logic
        // This is a placeholder to maintain compatibility with existing code
        logger.warn("evaluateAndSaveAttempt called but not fully implemented");
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("message", "Quiz evaluation not implemented yet");
        result.put("quizId", quiz.getId());
        return result;
    }
}