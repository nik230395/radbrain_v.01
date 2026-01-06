package org.nikolic.programm.services;

import org.nikolic.programm.dtos.*;
import org.nikolic.programm.entities.*;
import org.nikolic.programm.repositories.LearningAreaRepository;
import org.nikolic.programm.repositories.LearningContentRepository;
import org.nikolic.programm.repositories.LearningModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service für Lernbereich-Verwaltung
 */
@Service
public class LearningAreaService {

    @Autowired
    private LearningAreaRepository areaRepository;

    @Autowired
    private LearningModuleRepository moduleRepository;

    @Autowired
    private LearningContentRepository contentRepository;

    @Autowired
    private UserService userService;

    // ============================================================
    // User Methods - Lesezugriff
    // ============================================================

    /**
     * Alle veröffentlichten Lernbereiche abrufen
     */
    public List<LearningAreaDto> getAllPublishedAreas() {
        List<LearningArea> areas = areaRepository.findByIsPublishedOrderByDisplayOrderAsc(true);
        return areas.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Einzelnen Lernbereich mit allen Modulen abrufen
     */
    @Transactional(readOnly = true)
    public LearningAreaDto getAreaBySlug(String slug) {
        LearningArea area = areaRepository.findBySlugWithModules(slug)
                .orElseThrow(() -> new RuntimeException("Lernbereich nicht gefunden: " + slug));

        if (!area.isPublished()) {
            throw new RuntimeException("Lernbereich ist nicht veröffentlicht");
        }

        return convertToDtoWithModules(area);
    }

    /**
     * Modul mit allen Contents abrufen
     */
    @Transactional(readOnly = true)
    public LearningModuleDto getModuleById(Long moduleId) {
        LearningModule module = moduleRepository.findByIdWithContents(moduleId)
                .orElseThrow(() -> new RuntimeException("Modul nicht gefunden: " + moduleId));

        return convertToModuleDto(module, true);
    }

    // ============================================================
    // Admin Methods - CRUD-Operationen
    // ============================================================

    /**
     * Alle Lernbereiche für Admin abrufen (inkl. unveröffentlichte)
     */
    public List<LearningAreaDto> getAllAreasForAdmin() {
        List<LearningArea> areas = areaRepository.findAllByOrderByDisplayOrderAsc();
        return areas.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lernbereich erstellen
     */
    @Transactional
    public LearningAreaDto createArea(CreateLearningAreaRequest request, String username) {
        // Slug-Validierung
        if (areaRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Slug existiert bereits: " + request.getSlug());
        }

        User creator = userService.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden: " + username));

        LearningArea area = new LearningArea();
        area.setName(request.getName());
        area.setSlug(request.getSlug());
        area.setDescription(request.getDescription());
        area.setSubtitle(request.getSubtitle());
        area.setIconClass(request.getIconClass());
        area.setColorTheme(request.getColorTheme() != null ? request.getColorTheme() : "#3b82f6");
        area.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        area.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : false);
        area.setCreatedBy(creator);

        LearningArea saved = areaRepository.save(area);
        return convertToDto(saved);
    }

    /**
     * Lernbereich aktualisieren
     */
    @Transactional
    public LearningAreaDto updateArea(Long areaId, CreateLearningAreaRequest request) {
        LearningArea area = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Lernbereich nicht gefunden: " + areaId));

        // Slug-Validierung (nur wenn geändert)
        if (!area.getSlug().equals(request.getSlug()) &&
                areaRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Slug existiert bereits: " + request.getSlug());
        }

        area.setName(request.getName());
        area.setSlug(request.getSlug());
        area.setDescription(request.getDescription());
        area.setSubtitle(request.getSubtitle());
        area.setIconClass(request.getIconClass());
        area.setColorTheme(request.getColorTheme());
        area.setDisplayOrder(request.getDisplayOrder());
        area.setIsPublished(request.getIsPublished());

        LearningArea saved = areaRepository.save(area);
        return convertToDto(saved);
    }

    /**
     * Lernbereich löschen
     */
    @Transactional
    public void deleteArea(Long areaId) {
        LearningArea area = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Lernbereich nicht gefunden: " + areaId));
        areaRepository.delete(area);
    }

    /**
     * Modul erstellen
     */
    @Transactional
    public LearningModuleDto createModule(Long areaId, CreateModuleRequest request) {
        LearningArea area = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Lernbereich nicht gefunden: " + areaId));

        LearningModule module = new LearningModule();
        module.setTitle(request.getTitle());
        module.setSubtitle(request.getSubtitle());
        module.setDescription(request.getDescription());
        module.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        module.setIcon(request.getIcon());
        module.setEstimatedDuration(request.getEstimatedDuration());
        module.setLearningArea(area);

        LearningModule saved = moduleRepository.save(module);
        return convertToModuleDto(saved, false);
    }

    /**
     * Modul aktualisieren
     */
    @Transactional
    public LearningModuleDto updateModule(Long moduleId, CreateModuleRequest request) {
        LearningModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Modul nicht gefunden: " + moduleId));

        module.setTitle(request.getTitle());
        module.setSubtitle(request.getSubtitle());
        module.setDescription(request.getDescription());
        module.setDisplayOrder(request.getDisplayOrder());
        module.setIcon(request.getIcon());
        module.setEstimatedDuration(request.getEstimatedDuration());

        LearningModule saved = moduleRepository.save(module);
        return convertToModuleDto(saved, false);
    }

    /**
     * Modul löschen
     */
    @Transactional
    public void deleteModule(Long moduleId) {
        LearningModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Modul nicht gefunden: " + moduleId));
        moduleRepository.delete(module);
    }

    /**
     * Content erstellen
     */
    @Transactional
    public LearningContentDto createContent(Long moduleId, CreateContentRequest request) {
        LearningModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Modul nicht gefunden: " + moduleId));

        LearningContent content = new LearningContent();
        content.setContentType(ContentType.fromString(request.getContentType()));
        content.setTitle(request.getTitle());
        content.setContentData(request.getContentData());
        content.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        content.setMetadata(request.getMetadata());
        content.setModule(module);

        LearningContent saved = contentRepository.save(content);
        return convertToContentDto(saved);
    }

    /**
     * Content aktualisieren
     */
    @Transactional
    public LearningContentDto updateContent(Long contentId, CreateContentRequest request) {
        LearningContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content nicht gefunden: " + contentId));

        content.setContentType(ContentType.fromString(request.getContentType()));
        content.setTitle(request.getTitle());
        content.setContentData(request.getContentData());
        content.setDisplayOrder(request.getDisplayOrder());
        content.setMetadata(request.getMetadata());

        LearningContent saved = contentRepository.save(content);
        return convertToContentDto(saved);
    }

    /**
     * Content löschen
     */
    @Transactional
    public void deleteContent(Long contentId) {
        LearningContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content nicht gefunden: " + contentId));
        contentRepository.delete(content);
    }

    // ============================================================
    // Helper Methods - Entity zu DTO Konvertierung
    // ============================================================

    private LearningAreaDto convertToDto(LearningArea area) {
        LearningAreaDto dto = new LearningAreaDto();
        dto.setId(area.getId());
        dto.setName(area.getName());
        dto.setSlug(area.getSlug());
        dto.setDescription(area.getDescription());
        dto.setSubtitle(area.getSubtitle());
        dto.setIconClass(area.getIconClass());
        dto.setColorTheme(area.getColorTheme());
        dto.setDisplayOrder(area.getDisplayOrder());
        dto.setIsPublished(area.getIsPublished());
        dto.setModuleCount(area.getModuleCount());
        dto.setCreatedAt(area.getCreatedAt());
        dto.setUpdatedAt(area.getUpdatedAt());
        return dto;
    }

    private LearningAreaDto convertToDtoWithModules(LearningArea area) {
        LearningAreaDto dto = convertToDto(area);
        List<LearningModuleDto> modules = area.getModules().stream()
                .map(m -> convertToModuleDto(m, false))
                .collect(Collectors.toList());
        dto.setModules(modules);
        return dto;
    }

    private LearningModuleDto convertToModuleDto(LearningModule module, boolean includeContents) {
        LearningModuleDto dto = new LearningModuleDto();
        dto.setId(module.getId());
        dto.setLearningAreaId(module.getLearningArea().getId());
        dto.setTitle(module.getTitle());
        dto.setSubtitle(module.getSubtitle());
        dto.setDescription(module.getDescription());
        dto.setDisplayOrder(module.getDisplayOrder());
        dto.setIcon(module.getIcon());
        dto.setEstimatedDuration(module.getEstimatedDuration());
        dto.setContentCount(module.getContentCount());
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());

        if (includeContents) {
            List<LearningContentDto> contents = module.getContents().stream()
                    .map(this::convertToContentDto)
                    .collect(Collectors.toList());
            dto.setContents(contents);
        }

        return dto;
    }

    private LearningContentDto convertToContentDto(LearningContent content) {
        LearningContentDto dto = new LearningContentDto();
        dto.setId(content.getId());
        dto.setModuleId(content.getModule().getId());
        dto.setContentType(content.getContentType().name());
        dto.setTitle(content.getTitle());
        dto.setContentData(content.getContentData());
        dto.setDisplayOrder(content.getDisplayOrder());
        dto.setMetadata(content.getMetadata());
        dto.setCreatedAt(content.getCreatedAt());
        dto.setUpdatedAt(content.getUpdatedAt());
        return dto;
    }
}