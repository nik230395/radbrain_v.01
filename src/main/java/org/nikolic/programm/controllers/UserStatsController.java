package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.QuizAttempt;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attempts")
@CrossOrigin(origins = "*")
public class UserStatsController {

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/attempts/my-stats
     * Returns user's quiz statistics
     */
    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStats(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Nicht authentifiziert"));
        }

        try {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

            List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByCompletedAtDesc(user.getId());

            // Filter nur completed attempts
            List<QuizAttempt> completed = attempts.stream()
                    .filter(a -> a.getCompletedAt() != null)
                    .collect(Collectors.toList());

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCompleted", completed.size());

            if (!completed.isEmpty()) {
                // Average score
                double avgScore = completed.stream()
                        .map(QuizAttempt::getScorePct)
                        .filter(Objects::nonNull)
                        .mapToDouble(BigDecimal::doubleValue)
                        .average()
                        .orElse(0.0);
                stats.put("averageScore", avgScore);

                // Best score
                double bestScore = completed.stream()
                        .map(QuizAttempt::getScorePct)
                        .filter(Objects::nonNull)
                        .mapToDouble(BigDecimal::doubleValue)
                        .max()
                        .orElse(0.0);
                stats.put("bestScore", bestScore);

                // Weekly completed (last 7 days)
                LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
                long weeklyCompleted = completed.stream()
                        .filter(a -> a.getCompletedAt().isAfter(weekAgo))
                        .count();
                stats.put("weeklyCompleted", weeklyCompleted);
            } else {
                stats.put("averageScore", 0);
                stats.put("bestScore", 0);
                stats.put("weeklyCompleted", 0);
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/attempts/my-recent
     * Returns user's recent quiz attempts (last 10)
     */
    @GetMapping("/my-recent")
    public ResponseEntity<?> getMyRecentAttempts(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Nicht authentifiziert"));
        }

        try {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

            List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByCompletedAtDesc(user.getId());

            List<Map<String, Object>> response = attempts.stream()
                    .limit(10)
                    .map(this::attemptToMap)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/attempts/by-category
     * Returns stats grouped by quiz category
     */
    @GetMapping("/by-category")
    public ResponseEntity<?> getStatsByCategory(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Nicht authentifiziert"));
        }

        try {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

            List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByCompletedAtDesc(user.getId());

            // Group by category
            Map<String, List<QuizAttempt>> byCategory = attempts.stream()
                    .filter(a -> a.getCompletedAt() != null && a.getQuiz() != null)
                    .collect(Collectors.groupingBy(
                            a -> a.getQuiz().getCategory() != null ? a.getQuiz().getCategory() : "Allgemein"
                    ));

            Map<String, Map<String, Object>> categoryStats = new HashMap<>();

            for (Map.Entry<String, List<QuizAttempt>> entry : byCategory.entrySet()) {
                String category = entry.getKey();
                List<QuizAttempt> categoryAttempts = entry.getValue();

                Map<String, Object> stats = new HashMap<>();
                stats.put("completed", categoryAttempts.size());

                double avgScore = categoryAttempts.stream()
                        .map(QuizAttempt::getScorePct)
                        .filter(Objects::nonNull)
                        .mapToDouble(BigDecimal::doubleValue)
                        .average()
                        .orElse(0.0);
                stats.put("avgScore", avgScore);

                categoryStats.put(category, stats);
            }

            return ResponseEntity.ok(categoryStats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> attemptToMap(QuizAttempt attempt) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", attempt.getId());
        map.put("startedAt", attempt.getStartedAt());
        map.put("completedAt", attempt.getCompletedAt());
        map.put("scorePct", attempt.getScorePct() != null ? attempt.getScorePct().doubleValue() : null);
        map.put("completed", attempt.getCompletedAt() != null);

        if (attempt.getQuiz() != null) {
            Map<String, Object> quizInfo = new HashMap<>();
            quizInfo.put("id", attempt.getQuiz().getId());
            quizInfo.put("title", attempt.getQuiz().getTitle());
            quizInfo.put("category", attempt.getQuiz().getCategory());
            map.put("quiz", quizInfo);
        }

        return map;
    }
}