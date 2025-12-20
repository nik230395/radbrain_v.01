package org.nikolic.programm.dtos;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Minimaler Request-DTO zum Erstellen/Updaten von Quizzes.
 * - questions: optional, Liste von Frage-Objekten (text, qtype, position, choices)
 *   Jede Frage kann als Map übergeben werden; Mapping erfolgt im Service.
 */
@Data
public class CreateQuizRequest {
    private String title;
    private String description;
    private Boolean isPublished;
    private List<Map<String, Object>> questions;
}