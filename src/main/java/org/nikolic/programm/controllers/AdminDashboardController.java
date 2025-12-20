package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin dashboard endpoints (user list, quiz summaries).
 * All endpoints under /api/secure/admin/* and should be protected by your security config.
 *
 * Hinweis: Die Endpoint-URL für die vollständige Quiz-Liste wurde auf /quizzes/all geändert,
 * um Konflikte mit AdminQuizController (/api/secure/admin/quizzes) zu vermeiden.
 */
@RestController
@RequestMapping("/api/secure/admin")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    // ToDo: Make this work and fix logic everywhere

    private final UserRepository userRepository;
    private final QuizService quizService;

    public AdminDashboardController(UserRepository userRepository, QuizService quizService) {
        this.userRepository = userRepository;
        this.quizService = quizService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String,Object>> out = users.stream().map(u -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("email", u.getEmail());
            m.put("fullname", u.getFullname());
            // adjust getter name if your entity uses created_at instead of createdAt
            try { m.put("is_active", u.getIs_active()); } catch (Exception ex) { m.put("is_active", null); }
            try { m.put("created_at", u.getCreated_at()); } catch (Exception ex) {
                try { m.put("created_at", u.getCreated_at()); } catch (Exception ex2) { m.put("created_at", null); }
            }
            return m;
        }).collect(Collectors.toList());
        Map<String,Object> resp = new HashMap<>();
        resp.put("count", out.size());
        resp.put("users", out);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/quizzes/summary")
    public ResponseEntity<?> quizSummaries(@RequestParam(name = "limit", required = false, defaultValue = "3") int limit) {
        List<Quiz> all = quizService.getAllQuizzes();
        List<Map<String,Object>> list = all.stream()
                .sorted(Comparator.comparing(q -> {
                    // try common getter names for created date
                    try { return Optional.ofNullable(q.getCreatedAt()).orElse(null); } catch (Exception ex) {
                        try { return Optional.ofNullable(q.getCreatedAt()).orElse(null); } catch (Exception ex2) { return null; }
                    }
                }, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(q -> {
                    Map<String,Object> m = new HashMap<>();
                    m.put("id", q.getId());
                    m.put("title", q.getTitle());
                    m.put("description", q.getDescription());
                    m.put("isPublished", q.getIsPublished());
                    // createdAt fallback handling
                    try { m.put("createdAt", q.getCreatedAt()); } catch (Exception ex) {
                        try { m.put("createdAt", q.getCreatedAt()); } catch (Exception ex2) { m.put("createdAt", null); }
                    }
                    return m;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * NOTE: changed path to /quizzes/all to avoid duplicate mapping with AdminQuizController.
     */
    @GetMapping("/quizzes/all")
    public ResponseEntity<?> allQuizzesForAdmin() {
        List<Quiz> all = quizService.getAllQuizzes();
        List<Map<String,Object>> list = all.stream().map(q -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", q.getId());
            m.put("title", q.getTitle());
            m.put("description", q.getDescription());
            m.put("isPublished", q.getIsPublished());
            try { m.put("createdAt", q.getCreatedAt()); } catch (Exception ex) {
                try { m.put("createdAt", q.getCreatedAt()); } catch (Exception ex2) { m.put("createdAt", null); }
            }
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}