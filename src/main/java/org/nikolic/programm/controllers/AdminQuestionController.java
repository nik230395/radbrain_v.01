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
@RequestMapping("/api/admin/questions") // Pfad an Frontend angepasst
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            // 1. Debug-Log: Was kommt an?
            System.out.println("DEBUG: Eingehendes Update für ID: " + id);

            // 2. Suche die Frage
            Question question = questionService.findById(id).orElse(null);

            if (question == null) {
                System.err.println("KRITISCH: ID " + id + " nicht in DB gefunden!");
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Question not found in Database",
                        "requestedId", id,
                        "hint", "Bitte Seite neu laden (F5), die ID ist veraltet."
                ));
            }

            // 3. Felder aktualisieren
            if (request.containsKey("text")) {
                question.setText(request.get("text").toString());
            }

            if (request.containsKey("qtype")) {
                try {
                    String typeStr = request.get("qtype").toString().toUpperCase().replace(" ", "_");
                    question.setQtype(QuestionType.valueOf(typeStr));
                } catch (Exception e) {
                    System.out.println("QType Mapping fehlgeschlagen, behalte alten Wert.");
                }
            }

            // 4. Speichern
            questionService.save(question);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Interner Fehler: " + e.getMessage()));
        }
    }
}