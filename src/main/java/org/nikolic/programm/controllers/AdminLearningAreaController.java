package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.*;
import org.nikolic.programm.services.LearningAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-Controller für Lernbereiche (CRUD-Operationen)
 */
@RestController
@RequestMapping("/api/admin/learning-areas")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')") // Nur für Admins
public class AdminLearningAreaController {

    @Autowired
    private LearningAreaService learningAreaService;

    // =============== LEARNING AREAS ===============

    /**
     * Alle Lernbereiche abrufen (auch unveröffentlichte)
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
     * Einzelnen Lernbereich per ID abrufen
     * GET /api/admin/learning-areas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LearningAreaDto> getAreaById(@PathVariable Long id) {
        try {
            LearningAreaDto area = learningAreaService.getAreaById(id); // gibts nicht
            return ResponseEntity.ok(area);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Neuen Lernbereich erstellen
     * POST /api/admin/learning-areas
     */
    @PostMapping
    public ResponseEntity<LearningAreaDto> createArea(@RequestBody LearningAreaDto areaDto) {
        try {
            LearningAreaDto created = learningAreaService.createArea(areaDto); //error
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lernbereich aktualisieren
     * PUT /api/admin/learning-areas/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<LearningAreaDto> updateArea(
            @PathVariable Long id,
            @RequestBody LearningAreaDto areaDto
    ) {
        try {
            LearningAreaDto updated = learningAreaService.updateArea(id, areaDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Veröffentlichungsstatus ändern (PATCH)
     * PATCH /api/admin/learning-areas/{id}/publish
     */
    @PatchMapping("/{id}/publish")
    public ResponseEntity<LearningAreaDto> togglePublish(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload
    ) {
        try {
            Boolean isPublished = payload.get("isPublished");
            if (isPublished == null) {
                return ResponseEntity.badRequest().build();
            }

            LearningAreaDto updated = learningAreaService.setPublishStatus(id, isPublished);//error
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =============== MODULES ===============

    /**
     * Modul erstellen
     * POST /api/admin/learning-areas/{areaId}/modules
     */
    @PostMapping("/{areaId}/modules")
    public ResponseEntity<LearningModuleDto> createModule(
            @PathVariable Long areaId,
            @RequestBody LearningModuleDto moduleDto
    ) {
        try {
            moduleDto.setLearningAreaId(areaId);
            LearningModuleDto created = learningAreaService.createModule(moduleDto);//error
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Modul aktualisieren
     * PUT /api/admin/learning-areas/modules/{moduleId}
     */
    @PutMapping("/modules/{moduleId}")
    public ResponseEntity<LearningModuleDto> updateModule(
            @PathVariable Long moduleId,
            @RequestBody LearningModuleDto moduleDto
    ) {
        try {
            LearningModuleDto updated = learningAreaService.updateModule(moduleId, moduleDto);//error
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =============== CONTENTS ===============

    /**
     * Alle Contents eines Moduls abrufen
     * GET /api/admin/learning-areas/modules/{moduleId}/contents
     */
    @GetMapping("/modules/{moduleId}/contents")
    public ResponseEntity<List<LearningContentDto>> getModuleContents(@PathVariable Long moduleId) {
        try {
            List<LearningContentDto> contents = learningAreaService.getContentsByModuleId(moduleId);
            return ResponseEntity.ok(contents);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Content erstellen
     * POST /api/admin/learning-areas/modules/{moduleId}/contents
     */
    @PostMapping("/modules/{moduleId}/contents")
    public ResponseEntity<LearningContentDto> createContent(
            @PathVariable Long moduleId,
            @RequestBody LearningContentDto contentDto
    ) {
        try {
            contentDto.setModuleId(moduleId);
            LearningContentDto created = learningAreaService.createContent(contentDto);//error
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Content aktualisieren
     * PUT /api/admin/learning-areas/contents/{contentId}
     */
    @PutMapping("/contents/{contentId}")
    public ResponseEntity<LearningContentDto> updateContent(
            @PathVariable Long contentId,
            @RequestBody LearningContentDto contentDto
    ) {
        try {
            LearningContentDto updated = learningAreaService.updateContent(contentId, contentDto);//error
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}