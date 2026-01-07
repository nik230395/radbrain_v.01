package org.nikolic.programm.controllers;

import jakarta.validation.Valid;
import org.nikolic.programm.dtos.ApiResponse;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.exceptions.AuthenticationFailedException;
import org.nikolic.programm.exceptions.InvalidCredentialsException;
import org.nikolic.programm.exceptions.ValidationException;
import org.nikolic.programm.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User Profile Controller
 */
@RestController
@RequestMapping("/api/users/me")
@CrossOrigin(origins = "*")
public class UserProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserProfileController(
            UserService userService,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get current user profile
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            Authentication authentication) {

        User user = userService.getAuthenticatedUser(authentication)
                .orElseThrow(() -> new AuthenticationFailedException("Nicht authentifiziert"));

        Map<String, Object> userData = Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullname", user.getFullname(),
                "role", user.getRoleString(),
                "isActive", user.isActive(),
                "createdAt", user.getCreatedAt()
        );

        return ResponseEntity.ok(ApiResponse.success(userData));
    }

    /**
     * Update profile
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @Valid @RequestBody Map<String, String> request,
            Authentication authentication) {

        User user = userService.getAuthenticatedUser(authentication)
                .orElseThrow(() -> new AuthenticationFailedException("Nicht authentifiziert"));

        String fullname = request.get("fullname");
        if (fullname == null || fullname.trim().isEmpty()) {
            throw new ValidationException("Name darf nicht leer sein");
        }

        user.setFullname(fullname.trim());
        userService.save(user);

        Map<String, Object> userData = Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullname", user.getFullname(),
                "role", user.getRoleString()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Profil aktualisiert", userData)
        );
    }

    /**
     * Change password
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody Map<String, String> request,
            Authentication authentication) {

        User user = userService.getAuthenticatedUser(authentication)
                .orElseThrow(() -> new AuthenticationFailedException("Nicht authentifiziert"));

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            throw new ValidationException("Beide Passwörter erforderlich");
        }

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Aktuelles Passwort ist falsch");
        }

        // Validate new password
        if (newPassword.length() < 8) {
            throw new ValidationException("Neues Passwort muss mindestens 8 Zeichen haben");
        }

        // Update password
        userService.changePassword(user.getId(), currentPassword, newPassword);

        return ResponseEntity.ok(
                ApiResponse.success("Passwort erfolgreich geändert")
        );
    }

    /**
     * Delete account
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteAccount(
            Authentication authentication) {

        User user = userService.getAuthenticatedUser(authentication)
                .orElseThrow(() -> new AuthenticationFailedException("Nicht authentifiziert"));

        userService.deactivateUser(user.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Account wurde deaktiviert")
        );
    }
}
