
package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.*;
import org.nikolic.programm.services.LearningAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-Controller für Lernbereich-Verwaltung
 */
@RestController
@RequestMapping("/api/admin/learning-areas")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLearningAreaController {

    @Autowired
    private LearningAreaService learningAreaService;

    /**
     * Alle Lernbereiche für Admin abrufen (inkl. unveröffentlichte)
     * GET /api/admin/learning-areas
     */
    @GetMapping
    public ResponseEntity<List<LearningAreaDto>> getAllAreas() {
        try {
            List<LearningAreaDto> areas = learningAreaService.getAllAreasForAdmin();
            return ResponseEntity.ok(areas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Einzelnen Lernbereich abrufen
     * GET /api/admin/learning-areas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LearningAreaDto> getAreaById(@PathVariable Long id) {
        try {
            LearningAreaDto area = learningAreaService.getAreaBySlug(
                    learningAreaService.getAllAreasForAdmin().stream()
                            .filter(a -> a.getId().equals(id))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Not found"))
                            .getSlug()
            );
            return ResponseEntity.ok(area);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lernbereich erstellen
     * POST /api/admin/learning-areas
     */
    @PostMapping
    public ResponseEntity<LearningAreaDto> createArea(
            @RequestBody CreateLearningAreaRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            LearningAreaDto created = learningAreaService.createArea(request, username);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lernbereich aktualisieren
     * PUT /api/admin/learning-areas/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<LearningAreaDto> updateArea(
            @PathVariable Long id,
            @RequestBody CreateLearningAreaRequest request) {
        try {
            LearningAreaDto updated = learningAreaService.updateArea(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lernbereich löschen
     * DELETE /api/admin/learning-areas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable Long id) {
        try {
            learningAreaService.deleteArea(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Modul erstellen
     * POST /api/admin/learning-areas/{areaId}/modules
     */
    @PostMapping("/{areaId}/modules")
    public ResponseEntity<LearningModuleDto> createModule(
            @PathVariable Long areaId,
            @RequestBody CreateModuleRequest request) {
        try {
            LearningModuleDto created = learningAreaService.createModule(areaId, request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Modul aktualisieren
     * PUT /api/admin/learning-areas/modules/{moduleId}
     */
    @PutMapping("/modules/{moduleId}")
    public ResponseEntity<LearningModuleDto> updateModule(
            @PathVariable Long moduleId,
            @RequestBody CreateModuleRequest request) {
        try {
            LearningModuleDto updated = learningAreaService.updateModule(moduleId, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Modul löschen
     * DELETE /api/admin/learning-areas/modules/{moduleId}
     */
    @DeleteMapping("/modules/{moduleId}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long moduleId) {
        try {
            learningAreaService.deleteModule(moduleId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================
    // Learning Content CRUD
    // ============================================================

    /**
     * Content erstellen
     * POST /api/admin/learning-areas/modules/{moduleId}/contents
     */
    @PostMapping("/modules/{moduleId}/contents")
    public ResponseEntity<LearningContentDto> createContent(
            @PathVariable Long moduleId,
            @RequestBody CreateContentRequest request) {
        try {
            LearningContentDto created = learningAreaService.createContent(moduleId, request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Content aktualisieren
     * PUT /api/admin/learning-areas/contents/{contentId}
     */
    @PutMapping("/contents/{contentId}")
    public ResponseEntity<LearningContentDto> updateContent(
            @PathVariable Long contentId,
            @RequestBody CreateContentRequest request) {
        try {
            LearningContentDto updated = learningAreaService.updateContent(contentId, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Content löschen
     * DELETE /api/admin/learning-areas/contents/{contentId}
     */
    @DeleteMapping("/contents/{contentId}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long contentId) {
        try {
            learningAreaService.deleteContent(contentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}