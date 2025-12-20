package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.QuizAttempt;
import org.nikolic.programm.services.QuizAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// REST-Controller für Quiz-Versuche
@RestController
@RequestMapping("/api/attempts")
@CrossOrigin(origins = "*")
public class QuizAttemptController {

    @Autowired
    private QuizAttemptService quizAttemptService;

    // Quiz-Versuch starten
    @PostMapping("/start")
    public ResponseEntity<?> startAttempt(@RequestBody Map<String, Long> request) {
        try {
            Long quizId = request.get("quizId");
            Long userId = request.get("userId");
            QuizAttempt attempt = quizAttemptService.startQuizAttempt(quizId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(attempt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Quiz-Versuch abschließen
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeAttempt(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        try {
            String answersJson = (String) request.get("answersJson");
            BigDecimal scorePct = new BigDecimal(request.get("scorePct").toString());

            QuizAttempt attempt = quizAttemptService.completeQuizAttempt(id, answersJson, scorePct);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Versuche eines Users
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuizAttempt>> getUserAttempts(@PathVariable Long userId) {
        return ResponseEntity.ok(quizAttemptService.getAttemptsByUser(userId));
    }
}