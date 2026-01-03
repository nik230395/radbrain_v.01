package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@CrossOrigin(origins = "*")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * PUT /api/users/me/update
     * Update user's fullname
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Nicht authentifiziert"));
        }

        try {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

            String fullname = request.get("fullname");
            if (fullname == null || fullname.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Name darf nicht leer sein"));
            }

            user.setFullname(fullname.trim());
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("fullname", user.getFullname());
            response.put("role", user.getRole().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/users/me/change-password
     * Change user's password
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Nicht authentifiziert"));
        }

        try {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Beide Passwörter erforderlich"));
            }

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                return ResponseEntity.status(400).body(Map.of("error", "Aktuelles Passwort ist falsch"));
            }

            // Validate new password
            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest().body(Map.of("error", "Neues Passwort muss mindestens 8 Zeichen haben"));
            }

            // Update password
            user.changePassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Passwort erfolgreich geändert"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/users/me/delete
     * Delete user account (soft delete - set inactive)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Nicht authentifiziert"));
        }

        try {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

            // Soft delete - deactivate account
            user.deactivate();
            userRepository.save(user);

            // Alternative: Hard delete (uncomment if you want permanent deletion)
            // userRepository.delete(user);

            return ResponseEntity.ok(Map.of("message", "Account wurde gelöscht"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/users/me
     * Get current user info
     */
    @GetMapping
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Nicht authentifiziert"));
        }

        try {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("fullname", user.getFullname());
            response.put("role", user.getRole().toString());
            response.put("isActive", user.isActive());
            response.put("createdAt", user.getCreatedAt());
            response.put("updatedAt", user.getUpdatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}