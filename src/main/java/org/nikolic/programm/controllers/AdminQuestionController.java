package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.QuestionType;
import org.nikolic.programm.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/secure/admin/questions") // Pfad an Frontend angepasst
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody Map<String, Object> request) {
        try {
            // Validierung
            if (request.get("quizId") == null) return ResponseEntity.badRequest().body("quizId fehlt");

            Long quizId = Long.valueOf(request.get("quizId").toString());
            QuestionType qtype = QuestionType.valueOf(request.get("qtype").toString());
            String text = request.get("text").toString();
            String auxText = request.get("auxText") != null ? request.get("auxText").toString() : "";

            Question question = questionService.createQuestion(quizId, qtype, text, auxText);

            Map<String, Object> response = new HashMap<>();
            response.put("id", question.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}