package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.services.ChoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ✅ FIXED ChoiceController
 *
 * Fixed to return proper JSON without circular references
 */
@RestController
@RequestMapping("/api/choices")
@CrossOrigin(origins = "*")
public class ChoiceController {

    @Autowired
    private ChoiceService choiceService;

    @PostMapping
    public ResponseEntity<?> createChoice(@RequestBody Map<String, Object> request) {
        try {
            // Detailed logging
            System.out.println("=== Creating Choice ===");
            System.out.println("Request: " + request);

            // Validate input
            if (request.get("questionId") == null) {
                System.err.println("ERROR: questionId is missing");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "questionId is required"));
            }

            if (request.get("text") == null || request.get("text").toString().trim().isEmpty()) {
                System.err.println("ERROR: text is missing or empty");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "text is required and cannot be empty"));
            }

            // Parse values
            Long questionId = Long.valueOf(request.get("questionId").toString());
            String text = request.get("text").toString().trim();
            Boolean isCorrect = request.get("isCorrect") != null ?
                    Boolean.valueOf(request.get("isCorrect").toString()) : false;

            System.out.println("Parsed - questionId: " + questionId + ", text: " + text + ", isCorrect: " + isCorrect);

            // Create choice using service
            Choice choice = choiceService.createChoice(questionId, text, isCorrect);

            System.out.println("✅ Choice created with ID: " + choice.getId());

            // Return as simple JSON map to avoid circular references
            Map<String, Object> response = new HashMap<>();
            response.put("id", choice.getId());
            response.put("questionId", questionId);
            response.put("text", choice.getText());
            response.put("isCorrect", choice.getIsCorrect());
            response.put("position", choice.getPosition());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (NumberFormatException nfe) {
            System.err.println("ERROR: Invalid number format - " + nfe.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid questionId format: " + nfe.getMessage()));

        } catch (IllegalArgumentException iae) {
            System.err.println("ERROR: Illegal argument - " + iae.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid input: " + iae.getMessage()));

        } catch (Exception e) {
            System.err.println("ERROR: Unexpected error - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create choice: " + e.getMessage()));
        }
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<?> getChoicesByQuestion(@PathVariable Long questionId) {
        try {
            List<Choice> choices = choiceService.getChoicesByQuestion(questionId);

            // Convert to simple JSON to avoid circular references
            List<Map<String, Object>> response = choices.stream()
                    .map(c -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", c.getId());
                        map.put("text", c.getText());
                        map.put("isCorrect", c.getIsCorrect());
                        map.put("position", c.getPosition());
                        return map;
                    })
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load choices: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChoice(@PathVariable Long id) {
        try {
            choiceService.deleteChoice(id);
            return ResponseEntity.ok(Map.of("message", "Antwortoption gelöscht"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}