package org.nikolic.programm.controllers;

import jakarta.validation.Valid;
import org.nikolic.programm.dtos.ApiResponse;
import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.exceptions.AuthenticationFailedException;
import org.nikolic.programm.services.QuizService;
import org.nikolic.programm.services.UserService;
import org.nikolic.programm.utils.QuizMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Admin Quiz Controller
 * Fixed to work with existing ApiResponse signature
 */
@RestController
@RequestMapping("/api/admin/quizzes")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuizController {

    private final QuizService quizService;
    private final UserService userService;

    public AdminQuizController(QuizService quizService, UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }

    /**
     * Get all quizzes (including unpublished)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizDto>>> getAllQuizzes() {
        List<QuizDto> quizzes = quizService.findAll().stream()
                .map(QuizMapper::toLightDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(quizzes));
    }

    /**
     * Get single quiz by ID (for editing)
     * Enhanced with better error messages
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizDto>> getQuizById(@PathVariable Long id) {
        try {
            // Check if quiz exists
            Quiz quiz = quizService.findByIdWithQuestions(id)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Quiz nicht gefunden: Das Quiz mit der ID " + id +
                                    " existiert nicht. Möglicherweise wurde es gelöscht."
                    ));

            QuizDto dto = QuizMapper.toDto(quiz);
            return ResponseEntity.ok(ApiResponse.success(dto));

        } catch (NoSuchElementException e) {
            // Quiz not found - return 404
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            // Other errors - return 500
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Interner Fehler beim Laden des Quiz: " + e.getMessage()));
        }
    }

    /**
     * Create quiz
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuizDto>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request,
            Authentication authentication) {

        try {
            User user = userService.getAuthenticatedUser(authentication)
                    .orElseThrow(() -> new AuthenticationFailedException("Nicht authentifiziert"));

            Quiz quiz = quizService.createFromRequest(request, user);
            QuizDto dto = QuizMapper.toDto(quiz);

            return ResponseEntity.status(201)
                    .body(ApiResponse.success("Quiz erstellt", dto));

        } catch (AuthenticationFailedException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Nicht authentifiziert: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Fehler beim Erstellen: " + e.getMessage()));
        }
    }

    /**
     * Update quiz
     * Enhanced with existence check
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizDto>> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuizRequest request) {

        try {
            // Check if quiz exists first
            if (!quizService.existsById(id)) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(
                                "Quiz nicht gefunden: Das Quiz mit der ID " + id +
                                        " existiert nicht. Möglicherweise wurde es gelöscht."
                        ));
            }

            Quiz quiz = quizService.updateFromRequest(id, request);
            QuizDto dto = QuizMapper.toDto(quiz);

            return ResponseEntity.ok(
                    ApiResponse.success("Quiz aktualisiert", dto)
            );

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Quiz nicht gefunden: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Update fehlgeschlagen: " + e.getMessage()));
        }
    }

    /**
     * Publish quiz
     */
    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<QuizDto>> publishQuiz(@PathVariable Long id) {
        try {
            // Check if quiz exists
            if (!quizService.existsById(id)) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(
                                "Quiz nicht gefunden: Das Quiz mit der ID " + id + " existiert nicht."
                        ));
            }

            Quiz quiz = quizService.setPublished(id, true);
            QuizDto dto = QuizMapper.toDto(quiz);

            return ResponseEntity.ok(
                    ApiResponse.success("Quiz veröffentlicht", dto)
            );

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Quiz nicht gefunden: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Fehler beim Veröffentlichen: " + e.getMessage()));
        }
    }

    /**
     * Unpublish quiz
     */
    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<QuizDto>> unpublishQuiz(@PathVariable Long id) {
        try {
            // Check if quiz exists
            if (!quizService.existsById(id)) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(
                                "Quiz nicht gefunden: Das Quiz mit der ID " + id + " existiert nicht."
                        ));
            }

            Quiz quiz = quizService.setPublished(id, false);
            QuizDto dto = QuizMapper.toDto(quiz);

            return ResponseEntity.ok(
                    ApiResponse.success("Quiz unveröffentlicht", dto)
            );

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Quiz nicht gefunden: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Fehler: " + e.getMessage()));
        }
    }

    /**
     * Delete quiz
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteQuiz(@PathVariable Long id) {
        try {
            // Check if quiz exists
            if (!quizService.existsById(id)) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(
                                "Quiz nicht gefunden: Das Quiz mit der ID " + id + " existiert nicht."
                        ));
            }

            quizService.deleteQuizById(id);

            return ResponseEntity.ok(
                    ApiResponse.success("Quiz erfolgreich gelöscht")
            );

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Löschen fehlgeschlagen: " + e.getMessage()));
        }
    }

    /**
     * Check if quiz exists (helper endpoint)
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkQuizExists(@PathVariable Long id) {
        boolean exists = quizService.existsById(id);

        String message = exists ? "Quiz existiert" : "Quiz existiert nicht";
        return ResponseEntity.ok(ApiResponse.success(message, exists));
    }
}