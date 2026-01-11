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
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizDto>> getQuizById(@PathVariable Long id) {
        // ✅ Verwende findByIdWithQuestions für eager-loading
        Quiz quiz = quizService.findByIdWithQuestions(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz nicht gefunden mit ID: " + id));

        QuizDto dto = QuizMapper.toDto(quiz);  // ← FULL DTO mit allen Fragen & Choices

        return ResponseEntity.ok(ApiResponse.success(dto));
    }


    /**
     * Create quiz
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuizDto>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request,
            Authentication authentication) {

        User user = userService.getAuthenticatedUser(authentication)
                .orElseThrow(() -> new AuthenticationFailedException("Nicht authentifiziert"));

        Quiz quiz = quizService.createFromRequest(request, user);
        QuizDto dto = QuizMapper.toDto(quiz);

        return ResponseEntity.status(201)
                .body(ApiResponse.success("Quiz erstellt", dto));
    }

    /**
     * Update quiz
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizDto>> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuizRequest request) {

        Quiz quiz = quizService.updateFromRequest(id, request);
        QuizDto dto = QuizMapper.toDto(quiz);

        return ResponseEntity.ok(
                ApiResponse.success("Quiz aktualisiert", dto)
        );
    }

    /**
     * Publish quiz
     */
    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<QuizDto>> publishQuiz(@PathVariable Long id) {
        Quiz quiz = quizService.setPublished(id, true);
        QuizDto dto = QuizMapper.toDto(quiz);

        return ResponseEntity.ok(
                ApiResponse.success("Quiz veröffentlicht", dto)
        );
    }

    /**
     * Unpublish quiz
     */
    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<QuizDto>> unpublishQuiz(@PathVariable Long id) {
        Quiz quiz = quizService.setPublished(id, false);
        QuizDto dto = QuizMapper.toDto(quiz);

        return ResponseEntity.ok(
                ApiResponse.success("Quiz unveröffentlicht", dto)
        );
    }

    /**
     * Delete quiz
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuizById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Quiz erfolgreich gelöscht")
        );
    }
}
