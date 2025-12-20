package org.nikolic.programm.services;

import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.dtos.StatisticsDto;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.repositories.QuizRepository;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public AdminDashboardService(UserRepository userRepository,
                                 QuizRepository quizRepository,
                                 QuizAttemptRepository quizAttemptRepository) {
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    // 1. Fetch All Users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Delete a User
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // 3. Fetch All Quizzes
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    // 4. Create a New Quiz
    public Quiz createQuiz(CreateQuizRequest createQuizRequest) {
        // Fetch the User entity by ID
        User creator = userRepository.findById(createQuizRequest.getCreatedBy())
                .orElseThrow(() -> new IllegalStateException("User not found with ID: " + createQuizRequest.getCreatedBy()));

        // Create and save the quiz
        Quiz quiz = new Quiz();
        quiz.setTitle(createQuizRequest.getTitle());
        quiz.setDescription(createQuizRequest.getDescription());
        quiz.setCategory(createQuizRequest.getCategory());
        quiz.setCreatedBy(creator);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setIsPublished(false);

        return quizRepository.save(quiz);
    }

    // 5. Delete a Quiz
    public void deleteQuiz(Long quizId) {
        quizRepository.deleteById(quizId);
    }

    // 6. Fetch Statistics
    public StatisticsDto getStatistics() {
        long totalUsers = userRepository.count();
        long totalQuizzes = quizRepository.count();
        long totalAttempts = quizAttemptRepository.count();

        return new StatisticsDto(totalUsers, totalQuizzes, totalAttempts);
    }
}