package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.LoginRequest;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.services.JwtService;
import org.nikolic.programm.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ✅ EMERGENCY FIX AuthController - Validation Removed Temporarily
 *
 * Changes:
 * - REMOVED @Valid annotations (causing Bad Request)
 * - REMOVED @Validated class annotation
 * - Manual validation instead
 * - Once spring-boot-starter-validation is added, restore @Valid
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * User login endpoint
     * ⚠️ TEMPORARY: No @Valid until validation dependency is added
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for email: {}", loginRequest.getEmail());

            // ✅ Manual validation (temporary)
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("E-Mail ist erforderlich"));
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Passwort ist erforderlich"));
            }

            String email = loginRequest.getEmail().toLowerCase().trim();
            String password = loginRequest.getPassword();

            // Check if user exists
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                logger.warn("Login failed: User not found for email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Ungültige Anmeldedaten"));
            }

            User user = userOpt.get();

            // Check if user is active
            if (!user.isActive()) {
                logger.warn("Login failed: User account is inactive: {}", email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Ihr Konto ist deaktiviert. Bitte kontaktieren Sie den Support."));
            }

            // Check if email is verified
            if (!user.isEmailVerified()) {
                logger.warn("Login failed: Email not verified for: {}", email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error", "E-Mail-Adresse noch nicht bestätigt",
                                "requiresVerification", true,
                                "email", email
                        ));
            }

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = jwtService.generateToken(user);

            logger.info("Login successful for user: {}", email);

            // Return success response
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "email", user.getEmail(),
                    "fullname", user.getFullname(),
                    "id", user.getId(),
                    "roles", user.getRoleString()
            ));

        } catch (BadCredentialsException e) {
            logger.warn("Login failed: Invalid credentials for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Ungültige Anmeldedaten"));

        } catch (Exception e) {
            logger.error("Login error for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Anmeldung fehlgeschlagen: " + e.getMessage()));
        }
    }

    /**
     * Validate JWT token
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Ungültiges Token-Format"));
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Token ungültig"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Benutzer nicht gefunden"));
            }

            User user = userOpt.get();

            // ✅ FIXED: isTokenValid expects User object, not String
            if (!jwtService.isTokenValid(token, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Token ungültig oder abgelaufen"));
            }

            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Konto ist deaktiviert"));
            }

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "email", user.getEmail(),
                    "fullname", user.getFullname(),
                    "role", user.getRoleString()
            ));

        } catch (Exception e) {
            logger.error("Token validation error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token-Validierung fehlgeschlagen"));
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        try {
            if (authentication != null && authentication.getName() != null) {
                logger.info("User logged out: {}", authentication.getName());
            }

            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(Map.of("message", "Erfolgreich abgemeldet"));

        } catch (Exception e) {
            logger.error("Logout error", e);
            return ResponseEntity.ok(Map.of("message", "Abmeldung abgeschlossen"));
        }
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Nicht angemeldet"));
            }

            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Benutzer nicht gefunden"));
            }

            User user = userOpt.get();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("fullname", user.getFullname());
            userInfo.put("role", user.getRoleString());
            userInfo.put("isActive", user.isActive());
            userInfo.put("createdAt", user.getCreatedAt());

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            logger.error("Error getting current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Fehler beim Abrufen der Benutzerdaten"));
        }
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Ungültiges Token-Format"));
            }

            String oldToken = authHeader.substring(7);
            String email = jwtService.extractUsername(oldToken);

            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Token ungültig"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Benutzer nicht gefunden"));
            }

            User user = userOpt.get();

            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Konto ist deaktiviert"));
            }

            // Generate new token
            String newToken = jwtService.generateToken(user);

            logger.info("Token refreshed for user: {}", email);

            return ResponseEntity.ok(Map.of(
                    "token", newToken,
                    "email", user.getEmail(),
                    "fullname", user.getFullname(),
                    "role", user.getRoleString()
            ));

        } catch (Exception e) {
            logger.error("Token refresh error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token-Aktualisierung fehlgeschlagen"));
        }
    }

    // Helper Methods

    /**
     * Create standardized error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}