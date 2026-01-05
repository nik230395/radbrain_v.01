package org.nikolic.programm.entities;

/**
 * Enum für verschiedene Content-Typen in Lernmodulen
 */
public enum ContentType {
    TEXT("Text", "Normaler Fließtext"),
    HEADING("Überschrift", "Kapitelüberschrift"),
    IMAGE("Bild", "Bild oder Grafik"),
    CODE("Code", "Code-Beispiel"),
    DIAGRAM("Diagramm", "Diagramm oder Schema"),
    VIDEO("Video", "Video-Embed"),
    QUOTE("Zitat", "Hervorgehobenes Zitat"),
    LIST("Liste", "Aufzählung oder Liste"),
    TABLE("Tabelle", "Tabelle mit Daten"),
    CALLOUT("Hinweis", "Hinweisbox oder Callout");

    private final String displayName;
    private final String description;

    ContentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static ContentType fromString(String type) {
        for (ContentType ct : ContentType.values()) {
            if (ct.name().equalsIgnoreCase(type)) {
                return ct;
            }
        }
        return TEXT; // Default
    }
}