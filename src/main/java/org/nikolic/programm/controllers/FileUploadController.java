package org.nikolic.programm.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class FileUploadController {

    // Speicherort auf dem PC/Server (außerhalb des Codes!)
    private final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Ordner erstellen falls nicht da
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) directory.mkdirs();

            // Eindeutigen Namen generieren
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);

            // Datei speichern
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // URL zurückgeben, unter der das Bild erreichbar ist
            return ResponseEntity.ok(Map.of("url", "/uploads/" + fileName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload fehlgeschlagen: " + e.getMessage());
        }
    }
}