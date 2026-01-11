package org.nikolic.programm.controllers;

import org.nikolic.programm.services.ChoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/choices")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminChoiceController {

    @Autowired
    private ChoiceService choiceService;

    @PostMapping
    public ResponseEntity<?> createChoice(@RequestBody Map<String, Object> request) {
        try {
            Long questionId = Long.valueOf(request.get("questionId").toString());
            String text = request.get("text").toString();
            boolean isCorrect = (boolean) request.get("isCorrect");

            choiceService.createChoice(questionId, text, isCorrect);
            return ResponseEntity.ok(Map.of("message", "Choice created"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateChoice(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String text = request.get("text").toString();
            boolean isCorrect = (boolean) request.get("isCorrect");

            choiceService.updateChoice(id, text, isCorrect);

            return ResponseEntity.ok(Map.of("message", "Choice updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChoice(@PathVariable Long id) {
        try {
            choiceService.deleteById(id);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Choice deleted"
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}