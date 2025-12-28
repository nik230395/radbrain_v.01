package org.nikolic.programm.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.nikolic.programm.entities.PasswordReset;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.PasswordResetRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * PasswordResetController - Mit PasswordReset Entity
 *
 * Änderungen:
 * - Verwendet PasswordReset Entity statt Transient Felder
 * - Bessere Security (IP-Tracking, Attempt-Limiting)
 * - Cleanup alter Resets
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepository userRepository,
                                   PasswordResetRepository passwordResetRepository,
                                   EmailService emailService,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Schritt 1: Forgot Password - Sendet Reset Code per Email
     */
    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request,
                                            HttpServletRequest httpRequest) {
        try {
            String email = request.get("email");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "E-Mail ist erforderlich"));
            }

            String cleanEmail = email.toLowerCase().trim();

            // Security: Don't reveal if user exists
            Optional<User> userOpt = userRepository.findByEmail(cleanEmail);
            if (userOpt.isEmpty()) {
                logger.info("Password reset requested for non-existent email: {}", cleanEmail);
                return ResponseEntity.ok(Map.of(
                        "message", "Falls ein Konto mit dieser E-Mail existiert, wurde ein Reset-Code gesendet"
                ));
            }

            User user = userOpt.get();

            // Prüfe ob User aktiv und verifiziert ist
            if (!user.isEmailVerified() || !user.isActive()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Konto ist nicht aktiviert"));
            }

            // ✅ NEU: Markiere alle alten Resets als verwendet (Sicherheit)
            passwordResetRepository.markAllAsUsedForUser(user, LocalDateTime.now());

            // ✅ NEU: Erstelle neuen PasswordReset
            String resetCode = generateVerificationCode();
            String ipAddress = getClientIP(httpRequest);

            PasswordReset passwordReset = new PasswordReset(user, resetCode, 10); // 10 Minuten
            passwordReset.setIpAddress(ipAddress);
            passwordResetRepository.save(passwordReset);

            logger.info("Generated password reset code for {}: {} (IP: {})",
                    cleanEmail, resetCode, ipAddress);

            // Sende Reset Email
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullname(), resetCode);

            logger.info("Password reset email sent to: {}", user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "Reset-Code wurde an Ihre E-Mail gesendet"
            ));

        } catch (Exception ex) {
            logger.error("Password reset request failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Fehler beim Senden des Reset-Codes"));
        }
    }

    /**
     * Schritt 2: Verify Reset Code - Validiert Code und gibt Token zurück
     */
    @PostMapping("/verify-reset-code")
    @Transactional
    public ResponseEntity<?> verifyResetCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");

            if (email == null || code == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "E-Mail und Code sind erforderlich"));
            }

            String cleanEmail = email.toLowerCase().trim();

            Optional<User> userOpt = userRepository.findByEmail(cleanEmail);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ungültiger Code"));
            }

            User user = userOpt.get();

            // ✅ NEU: Finde aktiven PasswordReset
            Optional<PasswordReset> resetOpt = passwordResetRepository.findActiveByUser(user);

            if (resetOpt.isEmpty()) {
                logger.warn("No active reset found for: {}", cleanEmail);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Kein aktiver Reset-Code gefunden"));
            }

            PasswordReset passwordReset = resetOpt.get();

            // Prüfe ob Code abgelaufen ist
            if (passwordReset.isExpired()) {
                logger.warn("Expired reset code attempt for: {}", cleanEmail);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Code ist abgelaufen"));
            }

            // Prüfe ob zu viele Versuche
            if (passwordReset.hasExceededMaxAttempts()) {
                logger.warn("Too many attempts for reset code: {}", cleanEmail);
                passwordReset.markAsUsed(); // Invalidiere
                passwordResetRepository.save(passwordReset);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Zu viele Versuche. Bitte fordern Sie einen neuen Code an"));
            }

            // Prüfe Code
            if (!code.equals(passwordReset.getResetCode())) {
                passwordReset.incrementAttempts();
                passwordResetRepository.save(passwordReset);

                logger.warn("Invalid reset code attempt for: {} (Attempt: {})",
                        cleanEmail, passwordReset.getAttempts());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ungültiger Code"));
            }

            // ✅ Code ist korrekt - Generiere Token für nächsten Schritt
            String resetToken = generateResetToken();
            passwordReset.generateToken(resetToken, 15); // 15 Minuten für Password-Änderung
            passwordResetRepository.save(passwordReset);

            logger.info("Reset code verified for: {}", cleanEmail);

            return ResponseEntity.ok(Map.of(
                    "message", "Code verifiziert",
                    "resetToken", resetToken
            ));

        } catch (Exception ex) {
            logger.error("Reset code verification failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Verifizierung fehlgeschlagen"));
        }
    }

    /**
     * Schritt 3: Reset Password - Setzt neues Passwort mit Token
     */
    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String resetToken = request.get("resetToken");
            String newPassword = request.get("newPassword");

            if (email == null || resetToken == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Alle Felder sind erforderlich"));
            }

            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Passwort muss mindestens 8 Zeichen haben"));
            }

            String cleanEmail = email.toLowerCase().trim();

            Optional<User> userOpt = userRepository.findByEmail(cleanEmail);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ungültiger Reset-Token"));
            }

            User user = userOpt.get();

            // ✅ NEU: Finde PasswordReset by Token
            Optional<PasswordReset> resetOpt = passwordResetRepository
                    .findByValidToken(resetToken, LocalDateTime.now());

            if (resetOpt.isEmpty()) {
                logger.warn("Invalid or expired reset token for: {}", cleanEmail);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ungültiger oder abgelaufener Reset-Token"));
            }

            PasswordReset passwordReset = resetOpt.get();

            // Verify this reset belongs to this user
            if (!passwordReset.getUser().getId().equals(user.getId())) {
                logger.error("Reset token user mismatch for: {}", cleanEmail);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ungültiger Reset-Token"));
            }

            // ✅ Setze neues Passwort
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // ✅ Markiere Reset als verwendet
            passwordReset.markAsUsed();
            passwordResetRepository.save(passwordReset);

            // ✅ Invalidiere alle anderen Resets für Sicherheit
            passwordResetRepository.markAllAsUsedForUser(user, LocalDateTime.now());

            logger.info("Password successfully reset for: {}", cleanEmail);

            return ResponseEntity.ok(Map.of(
                    "message", "Passwort erfolgreich geändert"
            ));

        } catch (Exception ex) {
            logger.error("Password reset failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Passwort konnte nicht geändert werden"));
        }
    }

    /**
     * Optional: Cleanup-Endpoint für alte Resets (kann per Scheduler aufgerufen werden)
     */
    @DeleteMapping("/admin/cleanup-resets")
    @Transactional
    public ResponseEntity<?> cleanupOldResets() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

            passwordResetRepository.deleteOldUsedResets(cutoff);
            passwordResetRepository.deleteExpiredResets(LocalDateTime.now().minusDays(1));

            logger.info("Cleaned up old password resets");

            return ResponseEntity.ok(Map.of("message", "Cleanup erfolgreich"));
        } catch (Exception ex) {
            logger.error("Cleanup failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Cleanup fehlgeschlagen"));
        }
    }

    // Helper Methods

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private String generateResetToken() {
        Random random = new Random();
        return String.format("%016x", random.nextLong()) +
                String.format("%016x", random.nextLong());
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}