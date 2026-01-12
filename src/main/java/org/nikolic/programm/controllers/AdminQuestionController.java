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

/**
 * Admin Question Controller
 * Handles CRUD operations for quiz questions
 */
@RestController
@RequestMapping("/api/admin/questions")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * Create a new question
     * POST /api/admin/questions
     */
    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody Map<String, Object> request) {
        try {
            // Validation
            if (request.get("quizId") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "quizId fehlt"));
            }
            if (request.get("text") == null || request.get("text").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "text darf nicht leer sein"));
            }

            Long quizId = Long.valueOf(request.get("quizId").toString());
            String qtypeStr = request.get("qtype") != null ? request.get("qtype").toString() : "MULTIPLE_CHOICE";
            QuestionType qtype = QuestionType.valueOf(qtypeStr);
            String text = request.get("text").toString().trim();
            String auxText = request.get("auxText") != null ? request.get("auxText").toString() : "";

            Question question = questionService.createQuestion(quizId, qtype, text, auxText);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", question.getId());
            response.put("message", "Question created successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Update a question
     * PUT /api/admin/questions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            // Find the question
            Question question = questionService.findById(id).orElse(null);

            if (question == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Question not found",
                        "requestedId", id,
                        "hint", "Die Frage existiert nicht mehr. Bitte Seite neu laden."
                ));
            }

            // Update text if provided
            if (request.containsKey("text")) {
                String newText = request.get("text").toString().trim();
                if (newText.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "text darf nicht leer sein"));
                }
                question.setText(newText);
            }

            // Update type if provided
            if (request.containsKey("qtype")) {
                try {
                    String typeStr = request.get("qtype").toString()
                            .toUpperCase()
                            .replace(" ", "_")
                            .replace("-", "_");
                    question.setQtype(QuestionType.valueOf(typeStr));
                } catch (IllegalArgumentException e) {
                    // If invalid type, keep the old value
                    System.out.println("Invalid question type provided, keeping old value");
                }
            }

            // Update auxText if provided
            if (request.containsKey("auxText")) {
                question.setAuxText(request.get("auxText").toString());
            }

            // Save the question
            Question updated = questionService.save(question);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Question updated successfully",
                    "id", updated.getId()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal server error: " + e.getMessage()
            ));
        }
    }

    /**
     * Delete a question
     * DELETE /api/admin/questions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        try {
            // Check if question exists
            Question question = questionService.findById(id).orElse(null);

            if (question == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Question not found",
                        "requestedId", id
                ));
            }

            // Delete the question (this should also cascade delete choices)
            questionService.deleteById(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Question deleted successfully"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to delete question: " + e.getMessage()
            ));
        }
    }

    /**
     * Get a single question by ID
     * GET /api/admin/questions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestion(@PathVariable Long id) {
        try {
            Question question = questionService.findById(id).orElse(null);

            if (question == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Question not found",
                        "requestedId", id
                ));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", question.getId());
            response.put("text", question.getText());
            response.put("qtype", question.getQtype().toString());
            response.put("auxText", question.getAuxText());
            response.put("position", question.getPosition());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal server error: " + e.getMessage()
            ));
        }
    }
}