package org.nikolic.programm.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminImageController {

    // Speicherort innerhalb deines Projekts
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Datei ist leer");
        }

        try {
            // Verzeichnis erstellen, falls nicht vorhanden
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Dateiname generieren (Zeitstempel + Name)
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Datei speichern
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // WICHTIG: Die URL zurückgeben, die das Frontend erwartet
            return ResponseEntity.ok(Map.of("url", "/uploads/" + fileName));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Fehler beim Speichern: " + e.getMessage());
        }
    }
}