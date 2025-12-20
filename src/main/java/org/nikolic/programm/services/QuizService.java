package org.nikolic.programm.services;

import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.QuizRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class QuizService {

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
}