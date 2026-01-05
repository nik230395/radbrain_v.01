package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.services.QuizService;
import org.nikolic.programm.services.UserService;
import org.nikolic.programm.utils.QuizMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/secure/admin/quizzes")
@CrossOrigin(origins = "*")
public class AdminQuizController {

    private final QuizService quizService;
    private final UserService userService;

    public AdminQuizController(QuizService quizService, UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }

    private boolean isAdmin(Authentication auth) {
        return userService.isAdmin(auth);
    }

    private Optional<User> getAuthenticatedUser(Authentication auth) {
        return userService.getAuthenticatedUser(auth);
    }

    @GetMapping
    public ResponseEntity<?> listAll(Authentication auth) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Zugriff verweigert - Admin-Berechtigung erforderlich"));
        }

        try {
            // Get ALL quizzes, not just published
            List<Quiz> all = quizService.findAll();
            List<QuizDto> dtos = all.stream()
                    .map(QuizMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Fehler beim Laden der Quizzes"));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(Authentication auth, @RequestBody CreateQuizRequest req) {
        Optional<User> user = getAuthenticatedUser(auth);
        if (user.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentifizierung erforderlich"));
        }

        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin-Berechtigung erforderlich"));
        }

        try {
            Quiz createdQuiz = quizService.createFromRequest(req, user.get());
            return ResponseEntity.status(201).body(QuizMapper.toDto(createdQuiz));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication auth, @PathVariable Long id, @RequestBody CreateQuizRequest req) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin-Berechtigung erforderlich"));
        }

        try {
            Quiz updatedQuiz = quizService.updateFromRequest(id, req);
            return ResponseEntity.ok(QuizMapper.toDto(updatedQuiz));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin-Berechtigung erforderlich"));
        }

        try {
            Quiz publishedQuiz = quizService.setPublished(id, true);
            return ResponseEntity.ok(QuizMapper.toDto(publishedQuiz));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublish(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin-Berechtigung erforderlich"));
        }

        try {
            Quiz unpublishedQuiz = quizService.setPublished(id, false);
            return ResponseEntity.ok(QuizMapper.toDto(unpublishedQuiz));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin-Berechtigung erforderlich"));
        }

        try {
            quizService.deleteQuizById(id);
            return ResponseEntity.ok(Map.of("message", "Quiz erfolgreich gelöscht"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}