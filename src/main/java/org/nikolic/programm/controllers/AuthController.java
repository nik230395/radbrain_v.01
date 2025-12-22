package org.nikolic. programm.controllers;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.services.UserService;
import org. nikolic.programm.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework. http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController. class);

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "E-Mail und Passwort sind erforderlich"));
        }

        try {
            logger.info("Login attempt for email: {}", email);

            // ✅ Korrigierter Aufruf - verwende authenticateUserDetailed für bessere Error-Messages
            UserService.AuthenticationResult result = userService.authenticateUserDetailed(email, password);

            if (! result.isSuccess()) {
                logger.warn("Login failed for {}: {}", email, result.getMessage());

                if (result.needsVerification()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of(
                                    "error", result.getMessage(),
                                    "requiresVerification", true,
                                    "email", email
                            ));
                }

                return ResponseEntity. status(HttpStatus.UNAUTHORIZED)
                        .body(Map. of("error", result.getMessage()));
            }

            User user = result.getUser();

            // Generate JWT token
            String token = jwtUtil.createToken(user.getEmail(), user.getId());

            logger.info("Login successful for user: {}", user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "login_success",
                    "token", token,
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "fullname", user.getFullname(),
                    "role", user. getRoleString()
            ));

        } catch (Exception ex) {
            logger.error("Login error for email: {}", email, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Anmeldung fehlgeschlagen"));
        }
    }

    /**
     * Alternative:  Einfacher Login-Endpoint mit Basic-Authentication
     */
    @PostMapping("/login-simple")
    public ResponseEntity<?> loginSimple(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity. badRequest().body(Map.of("error", "E-Mail und Passwort sind erforderlich"));
        }

        try {
            // ✅ Verwende die einfache authenticateUser Methode
            Optional<User> userOpt = userService.authenticateUser(email, password);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültige Anmeldedaten"));
            }

            User user = userOpt.get();
            String token = jwtUtil.createToken(user.getEmail(), user.getId());

            return ResponseEntity. ok(Map.of(
                    "message", "login_success",
                    "token", token,
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "fullname", user.getFullname(),
                    "role", user.getRoleString()
            ));

        } catch (Exception ex) {
            logger. error("Login error", ex);
            return ResponseEntity. status(HttpStatus.INTERNAL_SERVER_ERROR)
                    . body(Map.of("error", "Anmeldung fehlgeschlagen"));
        }
    }

    /**
     * Token-Validierung
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");

        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token ist erforderlich"));
        }

        try {
            if (jwtUtil.isValidToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                Optional<User> userOpt = userService.findByEmail(email);

                if (userOpt.isPresent() && userOpt.get().isActive() && userOpt.get().isEmailVerified()) {
                    User user = userOpt.get();
                    return ResponseEntity.ok(Map.of(
                            "valid", true,
                            "email", user.getEmail(),
                            "fullname", user.getFullname(),
                            "role", user. getRoleString()
                    ));
                }
            }

            return ResponseEntity.ok(Map.of("valid", false));

        } catch (Exception ex) {
            logger.error("Token validation error", ex);
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    /**
     * Logout (Client-side token deletion)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In einer JWT-basierten Implementierung erfolgt Logout client-side
        // Optional: Token-Blacklisting implementieren
        return ResponseEntity.ok(Map.of("message", "logout_success"));
    }

    /**
     * User-Info basierend auf Token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authorization header required"));
        }

        String token = authHeader.substring(7);

        try {
            if (jwtUtil.isValidToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                Optional<User> userOpt = userService.findByEmail(email);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    return ResponseEntity.ok(Map. of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "fullname", user.getFullname(),
                            "role", user.getRoleString(),
                            "isActive", user.isActive(),
                            "isEmailVerified", user.isEmailVerified()
                    ));
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));

        } catch (Exception ex) {
            logger.error("Get current user error", ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }
    }
}