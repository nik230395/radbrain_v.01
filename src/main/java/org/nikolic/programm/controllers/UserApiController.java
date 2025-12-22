package org. nikolic.programm.controllers;

import org.nikolic.programm. dtos.RegisterRequest;
import org.nikolic. programm.entities.User;
import org.nikolic.programm.security.JwtUtil;
import org.nikolic. programm.services. RegistrationCacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail. MailException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserApiController {

    private static final Logger logger = LoggerFactory.getLogger(UserApiController.class);

    private final RegistrationCacheService registrationCacheService;
    private final JwtUtil jwtUtil;

    public UserApiController(RegistrationCacheService registrationCacheService, JwtUtil jwtUtil) {
        this.registrationCacheService = registrationCacheService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<? > register(@RequestBody RegisterRequest request) {
        try {
            logger.info("Registration attempt for email: {}", request.getEmail());

            // ✅ Korrigierter Methodenaufruf
            registrationCacheService.createRegistrationAndSendCode(
                    request.getFullname(),
                    request.getEmail(),
                    request.getPassword()
            );

            logger.info("Registration successful for email: {}", request. getEmail());
            return ResponseEntity.ok(Map.of("message", "verification_sent"));

        } catch (IllegalArgumentException iae) {
            logger.warn("Registration validation failed:  {}", iae.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", iae.getMessage()));

        } catch (MailException mex) {
            logger.error("Email sending failed", mex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map. of("error", "E-Mail konnte nicht gesendet werden.  Bitte versuchen Sie es später erneut."));

        } catch (Exception ex) {
            logger.error("Registration failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registrierung fehlgeschlagen:  " + ex.getMessage()));
        }
    }

    /**
     * Verify endpoint:  prüft Code, erzeugt den User und liefert JWT zurück
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "E-Mail und Code sind erforderlich"));
        }

        try {
            logger.info("Verification attempt for email: {}", email);

            // ✅ User wird erstellt und verifiziert
            User created = registrationCacheService.verifyAndCreateUser(email, code);

            if (created == null) {
                logger. warn("Verification failed for email: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Ungültiger oder abgelaufener Code"));
            }

            // Create JWT token for the newly created user
            String token = jwtUtil.createToken(created.getEmail(), created.getId());

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "verified");
            resp.put("id", created.getId());
            resp.put("email", created.getEmail());
            resp.put("fullname", created.getFullname());
            resp.put("token", token);

            logger.info("Verification successful for email: {}", email);
            return ResponseEntity.ok(resp);

        } catch (Exception ex) {
            logger. error("Verification error for email: {}", email, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Verifizierung fehlgeschlagen:  " + ex.getMessage()));
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resend(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "E-Mail ist erforderlich"));
        }

        try {
            logger.info("Resend request for email: {}", email);

            registrationCacheService.resendCode(email);

            logger.info("Resend successful for email: {}", email);
            return ResponseEntity. ok(Map.of("message", "verification_sent"));

        } catch (IllegalArgumentException iae) {
            logger.warn("Resend failed - no pending registration:  {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Keine ausstehende Registrierung gefunden"));

        } catch (MailException mex) {
            logger.error("Email resend failed", mex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map. of("error", "E-Mail konnte nicht gesendet werden"));

        } catch (Exception ex) {
            logger.error("Resend error for email: {}", email, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Fehler beim Senden:  " + ex.getMessage()));
        }
    }

    /**
     * Optional: Status-Endpoint um pending registrations zu prüfen
     */
    @GetMapping("/registration-status/{email}")
    public ResponseEntity<? > getRegistrationStatus(@PathVariable String email) {
        try {
            boolean hasPending = registrationCacheService.hasVerificationPending(email);
            return ResponseEntity.ok(Map.of(
                    "hasPendingVerification", hasPending,
                    "email", email
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Status konnte nicht abgerufen werden"));
        }
    }

    /**
     * Optional: Admin-Endpoint für Registrierungs-Statistiken
     */
    @GetMapping("/admin/registration-stats")
    public ResponseEntity<?> getRegistrationStats() {
        try {
            RegistrationCacheService.RegistrationStatistics stats =
                    registrationCacheService.getStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception ex) {
            return ResponseEntity. status(HttpStatus.INTERNAL_SERVER_ERROR)
                    . body(Map.of("error", "Statistiken konnten nicht abgerufen werden"));
        }
    }
}