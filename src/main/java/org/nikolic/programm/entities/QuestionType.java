package org.nikolic.programm.entities;

// Fragetypen (Multiple Choice, etc.)
public enum QuestionType {
    SINGLE, // Einzelauswahl
    MULTIPLE, // Mehrfachauswahl
    TRUE_FALSE, // Wahr/Falsch
    SHORT_TEXT, // Freitext
    FILL_GAP, // Lückentext
    FLASHCARD // Lernkarte
}