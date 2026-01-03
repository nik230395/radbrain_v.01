package org.nikolic.programm.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.QuizService;
import org.nikolic.programm.utils.QuizMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Public endpoints for taking quizzes (get quiz, submit answers).
 */
@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public QuizController(QuizService quizService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.quizService = quizService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/published")
    public ResponseEntity<?> getPublishedQuizzes() {
        List<Quiz> publishedQuizzes = quizService.findAllPublished();
        List<QuizDto> dtos = publishedQuizzes.stream()
                .map(QuizMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuiz(@PathVariable Long id) {
        Optional<Quiz> q = quizService.findById(id);
        if (q.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "not_found"));
        }
        QuizDto dto = QuizMapper.toDto(q.get());
        return ResponseEntity.ok(dto);
    }
    @PostMapping
    public ResponseEntity<?> createQuiz(@RequestBody Map<String, Object> request, Authentication auth) {
        try {
            // Check if user is admin
            User user = null;
            if (auth != null) {
                String email = auth.getName();
                user = userRepository.findByEmail(email).orElse(null);

                // Verify admin role
                if (user == null || !user.getRoleString().contains("ADMIN")) {
                    return ResponseEntity.status(403)
                            .body(Map.of("error", "Admin-Berechtigung erforderlich"));
                }
            } else {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentifizierung erforderlich"));
            }

            // Extract data
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String category = (String) request.get("category");

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Titel ist erforderlich"));
            }

            // Create quiz using QuizService
            org.nikolic.programm.dtos.CreateQuizRequest req = new org.nikolic.programm.dtos.CreateQuizRequest();
            req.setTitle(title);
            req.setDescription(description);
            req.setCategory(category);

            Quiz createdQuiz = quizService.createFromRequest(req, user);
            QuizDto dto = QuizMapper.toDto(createdQuiz);

            return ResponseEntity.status(201).body(dto);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Fehler beim Erstellen: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        Optional<Quiz> qopt = quizService.findById(id);
        if (qopt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "not_found"));
        }
        Quiz quiz = qopt.get();

        User user = null;
        if (auth != null) {
            String email = auth.getName();
            user = userRepository.findByEmail(email).orElse(null);
        }

        Map<String, Object> rawAnswers = objectMapper.convertValue(body.get("answers"), Map.class);
        Map<Long, Object> structuredAnswers = new HashMap<>();
        if (rawAnswers != null) {
            for (Map.Entry<String, Object> entry : rawAnswers.entrySet()) {
                try {
                    Long questionId = Long.parseLong(entry.getKey());
                    structuredAnswers.put(questionId, entry.getValue());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        Map<String, Object> result = quizService.evaluateAndSaveAttempt(quiz, user, structuredAnswers);
        return ResponseEntity.ok(result);
    }
}