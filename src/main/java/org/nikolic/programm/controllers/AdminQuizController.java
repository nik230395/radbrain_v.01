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
                .map(User::isAdmin)
                .orElse(false);
    }

    private Optional<User> getAuthenticatedUser(Authentication auth) {
        if (auth == null) return Optional.empty();
        String email = auth.getName();
        return userRepository.findByEmail(email);
    }

    @GetMapping
    public ResponseEntity<?> listAll(Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        List<Quiz> all = quizService.getAllQuizzes(); // there is no method getAllQuizzes yet
        List<QuizDto> dtos = all.stream().map(QuizMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<?> create(Authentication auth, @RequestBody CreateQuizRequest req) {
        Optional<User> user = getAuthenticatedUser(auth);
        if (user.isEmpty()) return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));
        try {
            Quiz createdQuiz = quizService.createFromRequest(req, user.get());
            return ResponseEntity.status(201).body(QuizMapper.toDto(createdQuiz));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication auth, @PathVariable Long id, @RequestBody CreateQuizRequest req) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        try {
            Quiz updatedQuiz = quizService.updateFromRequest(id, req);
            return ResponseEntity.ok(QuizMapper.toDto(updatedQuiz));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        try {
            Quiz publishedQuiz = quizService.setPublished(id, true); // doesnt exist yet
            return ResponseEntity.ok(QuizMapper.toDto(publishedQuiz));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublish(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        try {
            Quiz unpublishedQuiz = quizService.setPublished(id, false);
            return ResponseEntity.ok(QuizMapper.toDto(unpublishedQuiz));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        try {
            quizService.deleteQuizById(id);
            return ResponseEntity.ok(Map.of("message", "deleted"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}