package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.*;
import org.nikolic.programm.dtos.LearningAreaDto;
import org.nikolic.programm.dtos.LearningModuleDto;
import org.nikolic.programm.services.LearningAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-Controller für Lernbereiche (Lesezugriff)
 */
@RestController
@RequestMapping("/api/learning-areas")
@CrossOrigin(origins = "*")
public class LearningAreaController {

    @Autowired
    private LearningAreaService learningAreaService;

    /**
     * Alle veröffentlichten Lernbereiche abrufen
     * GET /api/learning-areas
     */
    @GetMapping
    public ResponseEntity<List<LearningAreaDto>> getAllPublishedAreas() {
        try {
            List<LearningAreaDto> areas = learningAreaService.getAllPublishedAreas();
            return ResponseEntity.ok(areas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Einzelnen Lernbereich mit Modulen abrufen
     * GET /api/learning-areas/{slug}
     */
    @GetMapping("/{slug}")
    public ResponseEntity<LearningAreaDto> getAreaBySlug(@PathVariable String slug) {
        try {
            LearningAreaDto area = learningAreaService.getAreaBySlug(slug);
            return ResponseEntity.ok(area);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Modul mit allen Contents abrufen
     * GET /api/learning-areas/modules/{moduleId}
     */
    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<LearningModuleDto> getModuleById(@PathVariable Long moduleId) {
        try {
            LearningModuleDto module = learningAreaService.getModuleById(moduleId);
            return ResponseEntity.ok(module);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}