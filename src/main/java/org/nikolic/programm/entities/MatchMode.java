package org.nikolic.programm.entities;

// Vergleichsmodus für Freitextantworten
public enum MatchMode {
    EXACT("exact"),
    CASE_INSENSITIVE("case_insensitive"),
    CONTAINS("contains"),
    REGEX("regex");

    private final String value;

    MatchMode(String value) { this.value = value; }
    public String getValue() { return value; }
}