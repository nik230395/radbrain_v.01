package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org.nikolic.programm.repositories.QuizRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Statistics Controller
 * Provides real-time stats for the admin dashboard
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminStatsController {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserService userService;

    public AdminStatsController(
            UserRepository userRepository,
            QuizRepository quizRepository,
            QuizAttemptRepository quizAttemptRepository,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.userService = userService;
    }

    /**
     * Check if user is admin
     */
    private boolean isAdmin(Authentication auth) {
        return userService.isAdmin(auth);
    }

    /**
     * Get comprehensive dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication auth) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        try {
            Map<String, Object> stats = new HashMap<>();

            // Total users
            long totalUsers = userRepository.count();
            stats.put("totalUsers", totalUsers);

            // Users created in last 7 days (if createdAt exists)
            long usersThisWeek = 0;
            try {
                LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
                usersThisWeek = userRepository.countByCreatedAtAfter(weekAgo);
            } catch (Exception e) {
                // If createdAt field doesn't exist or is null, just count 0
                System.out.println("Could not count users by date: " + e.getMessage());
            }
            stats.put("usersThisWeek", usersThisWeek);

            // Total quizzes
            long totalQuizzes = quizRepository.count();
            stats.put("totalQuizzes", totalQuizzes);

            // Published quizzes
            long publishedQuizzes = quizRepository.countByIsPublished(true);
            stats.put("publishedQuizzes", publishedQuizzes);

            // Draft quizzes
            long draftQuizzes = totalQuizzes - publishedQuizzes;
            stats.put("draftQuizzes", draftQuizzes);

            // Total quiz attempts
            long totalAttempts = quizAttemptRepository.count();
            stats.put("totalAttempts", totalAttempts);

            // Attempts in last 30 days (if startedAt exists)
            long attemptsThisMonth = 0;
            try {
                LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
                attemptsThisMonth = quizAttemptRepository.countByStartedAtAfter(monthAgo);
            } catch (Exception e) {
                // If method doesn't exist, use total
                System.out.println("Could not count attempts by date: " + e.getMessage());
            }
            stats.put("attemptsThisMonth", attemptsThisMonth);

            // Average attempts per quiz
            double avgAttemptsPerQuiz = totalQuizzes > 0 ? (double) totalAttempts / totalQuizzes : 0;
            stats.put("avgAttemptsPerQuiz", Math.round(avgAttemptsPerQuiz * 10) / 10.0);

            // Admin count - count manually to avoid enum issues
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRoleString() != null && u.getRoleString().contains("ADMIN"))
                    .count();
            stats.put("adminCount", adminCount);

            // Current admin info
            Optional<User> currentAdmin = userService.getAuthenticatedUser(auth);
            if (currentAdmin.isPresent()) {
                Map<String, Object> adminInfo = new HashMap<>();
                adminInfo.put("id", currentAdmin.get().getId());
                adminInfo.put("email", currentAdmin.get().getEmail());
                adminInfo.put("fullname", currentAdmin.get().getFullname());
                adminInfo.put("role", currentAdmin.get().getRoleString());
                stats.put("currentAdmin", adminInfo);
            }

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load stats: " + e.getMessage()));
        }
    }

    /**
     * Get all users (admin only)
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication auth) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        try {
            List<User> users = userRepository.findAll();

            List<Map<String, Object>> userList = users.stream()
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", user.getId());
                        userMap.put("email", user.getEmail());
                        userMap.put("fullname", user.getFullname());
                        userMap.put("role", user.getRoleString());
                        userMap.put("isActive", user.isActive());
                        userMap.put("createdAt", user.getCreatedAt());
                        return userMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userList);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load users: " + e.getMessage()));
        }
    }

    /**
     * Get recent activity log
     */
    @GetMapping("/activity")
    public ResponseEntity<?> getRecentActivity(Authentication auth) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        try {
            List<Map<String, Object>> activities = new ArrayList<>();

            // Get recent quiz attempts (last 20)
            var recentAttempts = quizAttemptRepository.findTop20ByOrderByStartedAtDesc();

            for (var attempt : recentAttempts) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "QUIZ_ATTEMPT");
                activity.put("user", attempt.getUser().getEmail());
                activity.put("quiz", attempt.getQuiz().getTitle());
                activity.put("score", attempt.getScorePct());
                activity.put("timestamp", attempt.getStartedAt());
                activities.add(activity);
            }

            // Get recently created quizzes (last 10)
            var recentQuizzes = quizRepository.findTop10ByOrderByCreatedAtDesc();

            for (var quiz : recentQuizzes) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "QUIZ_CREATED");
                activity.put("user", quiz.getCreatedBy().getEmail());
                activity.put("quiz", quiz.getTitle());
                activity.put("timestamp", quiz.getCreatedAt());
                activities.add(activity);
            }

            // Sort by timestamp
            activities.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
                LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
                return timeB.compareTo(timeA);
            });

            return ResponseEntity.ok(activities.stream().limit(30).collect(Collectors.toList()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load activity: " + e.getMessage()));
        }
    }

    /**
     * Get user count by role
     */
    @GetMapping("/users/by-role")
    public ResponseEntity<?> getUsersByRole(Authentication auth) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        try {
            Map<String, Long> roleCount = new HashMap<>();

            // Count manually to avoid enum issues
            List<User> allUsers = userRepository.findAll();
            long adminCount = allUsers.stream()
                    .filter(u -> u.getRoleString() != null && u.getRoleString().contains("ADMIN"))
                    .count();
            long userCount = allUsers.size() - adminCount;

            roleCount.put("ADMIN", adminCount);
            roleCount.put("USER", userCount);

            return ResponseEntity.ok(roleCount);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load role stats: " + e.getMessage()));
        }
    }

    /**
     * Get quiz statistics by category
     */
    @GetMapping("/quizzes/by-category")
    public ResponseEntity<?> getQuizzesByCategory(Authentication auth) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        try {
            Map<String, Long> categoryCount = new HashMap<>();

            List<String> categories = Arrays.asList("ROENTGEN", "CT", "MRT", "ULTRASCHALL");
            for (String category : categories) {
                long count = quizRepository.countByCategory(category);
                categoryCount.put(category, count);
            }

            // Count quizzes without category
            long uncategorized = quizRepository.countByCategoryIsNull();
            categoryCount.put("UNCATEGORIZED", uncategorized);

            return ResponseEntity.ok(categoryCount);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load category stats: " + e.getMessage()));
        }
    }
}