package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.services.UserService;
import org.nikolic.programm.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AuthController - Mit expliziten Content-Type Headers
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Login - Mit explizitem JSON Content-Type
     */
    @PostMapping(value = "/login",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        logger.info("🔐 Login request received");

        String email = body.get("email");
        String password = body.get("password");

        // Validation
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "E-Mail ist erforderlich");
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        if (password == null || password.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Passwort ist erforderlich");
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        try {
            logger.info("📧 Attempting login for: {}", email);

            // Verwende detaillierte Authentifizierung
            UserService.AuthenticationResult result = userService.authenticateUserDetailed(email, password);

            if (!result.isSuccess()) {
                logger.warn("❌ Login failed for {}: {}", email, result.getMessage());

                Map<String, Object> error = new HashMap<>();
                error.put("error", result.getMessage());
                error.put("timestamp", System.currentTimeMillis());

                if (result.needsVerification()) {
                    error.put("requiresVerification", true);
                    error.put("email", email);
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(error);
                }

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
            }

            User user = result.getUser();

            // Generate JWT token
            String token = jwtUtil.createToken(user.getEmail(), user.getId());

            // ✅ SUCCESS RESPONSE
            Map<String, Object> response = new HashMap<>();
            response.put("message", "login_success");
            response.put("token", token);
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("fullname", user.getFullname());
            response.put("roles", user.getRoleString());
            response.put("timestamp", System.currentTimeMillis());

            logger.info("✅ Login successful for: {}", email);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (Exception ex) {
            logger.error("💥 Login error for {}: {}", email, ex.getMessage(), ex);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Anmeldung fehlgeschlagen");
            error.put("timestamp", System.currentTimeMillis());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    /**
     * Token-Validierung
     */
    @PostMapping(value = "/validate",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");

        if (token == null || token.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Token ist erforderlich");
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        try {
            if (jwtUtil.isValidToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                Optional<User> userOpt = userService.findByEmail(email);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    if (user.isActive() && user.isEmailVerified()) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("valid", true);
                        response.put("email", user.getEmail());
                        response.put("fullname", user.getFullname());
                        response.put("roles", user.getRoleString());
                        response.put("timestamp", System.currentTimeMillis());

                        return ResponseEntity
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(response);
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (Exception ex) {
            logger.error("Token validation error", ex);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    /**
     * Logout
     */
    @PostMapping(value = "/logout",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "logout_success");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * User-Info basierend auf Token
     */
    @GetMapping(value = "/me",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Authorization header required");
            error.put("timestamp", System.currentTimeMillis());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        String token = authHeader.substring(7);

        try {
            if (jwtUtil.isValidToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                Optional<User> userOpt = userService.findByEmail(email);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("email", user.getEmail());
                    response.put("fullname", user.getFullname());
                    response.put("roles", user.getRoleString());
                    response.put("isActive", user.isActive());
                    response.put("isEmailVerified", user.isEmailVerified());
                    response.put("timestamp", System.currentTimeMillis());

                    return ResponseEntity
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(response);
                }
            }

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid token");
            error.put("timestamp", System.currentTimeMillis());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);

        } catch (Exception ex) {
            logger.error("Get current user error", ex);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid token");
            error.put("timestamp", System.currentTimeMillis());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }
}