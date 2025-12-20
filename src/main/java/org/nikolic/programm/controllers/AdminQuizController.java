package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.QuizService;
import org.nikolic.programm.utils.QuizMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Admin controller to manage quizzes.
 * Protected endpoints should be accessed with an Admin JWT.
 */
@RestController
@RequestMapping("/api/secure/admin/quizzes")
@CrossOrigin(origins = "*")
public class AdminQuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;

    public AdminQuizController(QuizService quizService, UserRepository userRepository) {
        this.quizService = quizService;
        this.userRepository = userRepository;
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(u -> {
                    try {
                        return u.getRoles() != null && u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
                    } catch (Exception ex) {
                        return "ADMIN".equalsIgnoreCase(u.getRole());
                    }
                }).orElse(false);
    }

    private Optional<User> getAuthenticatedUser(Authentication auth) {
        if (auth == null) return Optional.empty();
        String email = auth.getName();
        return userRepository.findByEmail(email);
    }

    @GetMapping
    public ResponseEntity<?> listAll(Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        List<Quiz> all = quizService.getAllQuizzes();
        List<QuizDto> dtos = all.stream().map(QuizMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<?> create(Authentication auth, @RequestBody CreateQuizRequest req) {
        Optional<User> u = getAuthenticatedUser(auth);
        if (u.isEmpty()) return ResponseEntity.status(401).body(Map.of("error","unauthenticated"));
        try {
            Quiz created = quizService.createFromRequest(req, u.get());
            return ResponseEntity.status(201).body(QuizMapper.toDto(created));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication auth, @PathVariable Long id, @RequestBody CreateQuizRequest req) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        try {
            Quiz updated = quizService.updateFromRequest(id, req);
            return ResponseEntity.ok(QuizMapper.toDto(updated));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        try {
            Quiz q = quizService.setPublished(id, true);
            return ResponseEntity.ok(QuizMapper.toDto(q));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublish(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        try {
            Quiz q = quizService.setPublished(id, false);
            return ResponseEntity.ok(QuizMapper.toDto(q));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.ok(Map.of("message","deleted"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}