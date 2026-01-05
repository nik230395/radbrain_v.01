package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.QuizAttempt;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org.nikolic.programm.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for user statistics and dashboard data
 */
@RestController
@RequestMapping("/api/attempts")
@CrossOrigin(origins = "*")
public class UserStatsController {

    private final QuizAttemptRepository attemptRepository;
    private final UserService userService;

    public UserStatsController(QuizAttemptRepository attemptRepository, UserService userService) {
        this.attemptRepository = attemptRepository;
        this.userService = userService;
    }

    /**
     * GET /api/attempts/my-stats
     * Returns user's overall statistics
     */
    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStats(Authentication auth) {
        Optional<User> userOpt = userService.getAuthenticatedUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = userOpt.get();

        // Get all completed attempts
        List<QuizAttempt> attempts = attemptRepository.findByUserAndCompletedAtNotNull(user);

        // Calculate stats
        long totalCompleted = attempts.size();

        BigDecimal averageScore = BigDecimal.ZERO;
        if (!attempts.isEmpty()) {
            BigDecimal sum = attempts.stream()
                    .map(QuizAttempt::getScorePct)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageScore = sum.divide(BigDecimal.valueOf(totalCompleted), 2, RoundingMode.HALF_UP);
        }

        BigDecimal bestScore = attempts.stream()
                .map(QuizAttempt::getScorePct)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // Weekly completed (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long weeklyCompleted = attempts.stream()
                .filter(a -> a.getCompletedAt() != null && a.getCompletedAt().isAfter(weekAgo))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCompleted", totalCompleted);
        stats.put("averageScore", averageScore.doubleValue());
        stats.put("bestScore", bestScore.doubleValue());
        stats.put("weeklyCompleted", weeklyCompleted);

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/attempts/my-recent
     * Returns user's 10 most recent quiz attempts
     */
    @GetMapping("/my-recent")
    public ResponseEntity<?> getMyRecent(Authentication auth) {
        Optional<User> userOpt = userService.getAuthenticatedUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = userOpt.get();

        // Get all completed attempts ordered by completedAt desc
        List<QuizAttempt> attempts = attemptRepository.findByUserAndCompletedAtNotNullOrderByCompletedAtDesc(user);

        // Take first 10
        List<Map<String, Object>> recent = attempts.stream()
                .limit(10)
                .map(attempt -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", attempt.getId());
                    item.put("quizId", attempt.getQuiz().getId());
                    item.put("quizTitle", attempt.getQuiz().getTitle());
                    item.put("category", attempt.getQuiz().getCategory());
                    item.put("scorePct", attempt.getScorePct() != null ? attempt.getScorePct().doubleValue() : 0.0);
                    item.put("completedAt", attempt.getCompletedAt().toString());
                    item.put("completed", true);
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(recent);
    }

    /**
     * GET /api/attempts/by-category
     * Returns stats grouped by quiz category
     */
    @GetMapping("/by-category")
    public ResponseEntity<?> getByCategory(Authentication auth) {
        Optional<User> userOpt = userService.getAuthenticatedUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = userOpt.get();

        // Get all completed attempts
        List<QuizAttempt> attempts = attemptRepository.findByUserAndCompletedAtNotNull(user);

        // Group by category
        Map<String, List<QuizAttempt>> byCategory = attempts.stream()
                .filter(a -> a.getQuiz().getCategory() != null)
                .collect(Collectors.groupingBy(a -> a.getQuiz().getCategory()));

        // Calculate stats per category
        Map<String, Map<String, Object>> categoryStats = new HashMap<>();

        for (Map.Entry<String, List<QuizAttempt>> entry : byCategory.entrySet()) {
            String category = entry.getKey();
            List<QuizAttempt> categoryAttempts = entry.getValue();

            long completed = categoryAttempts.size();

            BigDecimal avgScore = BigDecimal.ZERO;
            if (!categoryAttempts.isEmpty()) {
                BigDecimal sum = categoryAttempts.stream()
                        .map(QuizAttempt::getScorePct)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                avgScore = sum.divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP);
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("completed", completed);
            stats.put("avgScore", avgScore.doubleValue());

            categoryStats.put(category, stats);
        }

        return ResponseEntity.ok(categoryStats);
    }
}