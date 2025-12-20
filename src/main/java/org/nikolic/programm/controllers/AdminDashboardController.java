package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.services.AdminDashboardService;
import org.nikolic.programm.dtos.StatisticsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // Base URL for Admin APIs
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    // 1. Fetch All Users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = adminDashboardService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // 2. Delete a User
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminDashboardService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // 3. Fetch All Quizzes
    @GetMapping("/quizzes")
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        List<Quiz> quizzes = adminDashboardService.getAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    // 4. Create a New Quiz
    @PostMapping("/quizzes")
    public ResponseEntity<Quiz> createQuiz(@RequestBody CreateQuizRequest createQuizRequest) {
        Quiz quiz = adminDashboardService.createQuiz(createQuizRequest);
        return ResponseEntity.ok(quiz);
    }

    // 5. Delete a Quiz
    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        adminDashboardService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    // 6. Fetch Statistics
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsDto> getStatistics() {
        StatisticsDto statistics = adminDashboardService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
}