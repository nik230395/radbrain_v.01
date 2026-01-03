package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.QuestionType;
import org.nikolic.programm.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ✅ FIXED QuestionController
 *
 * Now returns proper JSON without circular references
 */
@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody Map<String, Object> request) {
        try {
            // Logging
            System.out.println("=== Creating Question ===");
            System.out.println("Request: " + request);

            // Validate
            if (request.get("quizId") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "quizId is required"));
            }
            if (request.get("qtype") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "qtype is required"));
            }
            if (request.get("text") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "text is required"));
            }

            Long quizId = Long.valueOf(request.get("quizId").toString());
            QuestionType qtype = QuestionType.valueOf(request.get("qtype").toString().toUpperCase());
            String text = (String) request.get("text");
            String auxText = request.get("auxText") != null ? (String) request.get("auxText") : "";

            System.out.println("Parsed - quizId: " + quizId + ", qtype: " + qtype + ", text: " + text);

            Question question = questionService.createQuestion(quizId, qtype, text, auxText);

            System.out.println("✅ Question created with ID: " + question.getId());

            // ✅ CRITICAL: Return simple JSON with ID!
            Map<String, Object> response = new HashMap<>();
            response.put("id", question.getId());
            response.put("quizId", quizId);
            response.put("text", question.getText());
            response.put("qtype", question.getQtype().toString());
            response.put("auxText", question.getAuxText());
            response.put("position", question.getPosition());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: Invalid argument - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected error - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create question: " + e.getMessage()));
        }
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<?> getQuestionsByQuiz(@PathVariable Long quizId) {
        try {
            List<Question> questions = questionService.getQuestionsByQuiz(quizId);

            // Convert to simple JSON
            List<Map<String, Object>> response = questions.stream()
                    .map(q -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", q.getId());
                        map.put("text", q.getText());
                        map.put("qtype", q.getQtype().toString());
                        map.put("auxText", q.getAuxText());
                        map.put("position", q.getPosition());
                        return map;
                    })
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load questions: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable Long id) {
        try {
            return questionService.getQuestionById(id)
                    .map(q -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", q.getId());
                        map.put("text", q.getText());
                        map.put("qtype", q.getQtype().toString());
                        map.put("auxText", q.getAuxText());
                        map.put("position", q.getPosition());
                        return ResponseEntity.ok((Object) map);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String auxText = request.get("auxText");
            Question question = questionService.updateQuestion(id, text, auxText);

            Map<String, Object> response = new HashMap<>();
            response.put("id", question.getId());
            response.put("text", question.getText());
            response.put("qtype", question.getQtype().toString());
            response.put("auxText", question.getAuxText());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok(Map.of("message", "Frage gelöscht"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}