package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.services.ChoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// REST-Controller für Antwortmöglichkeiten (Choice)
@RestController
@RequestMapping("/api/choices")
@CrossOrigin(origins = "*")
public class ChoiceController {

    @Autowired
    private ChoiceService choiceService;

    // Neue Antwortoption
    @PostMapping
    public ResponseEntity<?> createChoice(@RequestBody Map<String, Object> request) {
        try {
            Long questionId = Long.valueOf(request.get("questionId").toString());
            String text = (String) request.get("text");
            Boolean isCorrect = Boolean.valueOf(request.get("isCorrect").toString());

            Choice choice = choiceService.createChoice(questionId, text, isCorrect);
            return ResponseEntity.status(HttpStatus.CREATED).body(choice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Antwortoptionen zu einer Frage
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<Choice>> getChoicesByQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(choiceService.getChoicesByQuestion(questionId));
    }

    // Antwortoption löschen
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChoice(@PathVariable Long id) {
        try {
            choiceService.deleteChoice(id);
            return ResponseEntity.ok(Map.of("message", "Antwortoption gelöscht"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}