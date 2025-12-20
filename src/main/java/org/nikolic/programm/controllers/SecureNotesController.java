package org.nikolic.programm.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Example secured controller showing how to read authenticated user from SecurityContext.
 * Stores notes in-memory (demo). Replace with DB persistence for production.
 */
@RestController
@RequestMapping("/api/secure/notes")
public class SecureNotesController {

    // simple in-memory store: email -> list of notes
    private final Map<String, List<String>> store = new HashMap<>();

    @GetMapping
    public ResponseEntity<?> getNotes(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));
        String email = (String) authentication.getPrincipal();
        List<String> notes = store.getOrDefault(email, List.of());
        return ResponseEntity.ok(Map.of("notes", notes));
    }

    @PostMapping
    public ResponseEntity<?> addNote(Authentication authentication, @RequestBody Map<String, String> body) {
        if (authentication == null) return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));
        String email = (String) authentication.getPrincipal();
        String text = body.get("text");
        if (text == null || text.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "text required"));
        store.computeIfAbsent(email, k -> new ArrayList<>()).add(text);
        return ResponseEntity.ok(Map.of("message", "saved"));
    }
}