package org. nikolic.programm.controllers;

import org.nikolic.programm.entities.User;
import org.nikolic.programm. repositories.UserRepository;
import org. nikolic.programm.services.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java. util.Map;
import java. util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepository userRepository,
                                   EmailService emailService,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this. passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request. get("email");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "E-Mail ist erforderlich"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email. toLowerCase().trim());
            if (userOpt.isEmpty()) {
                // Don't reveal that user doesn't exist for security
                return ResponseEntity.ok(Map.of("message", "Falls ein Konto mit dieser E-Mail existiert, wurde ein Reset-Code gesendet"));
            }

            User user = userOpt.get();

            // ✅ Korrigierte Methode - verwende isActive() statt getIs_Active()
            if (!user.isEmailVerified() || ! user.isActive()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Konto ist nicht aktiviert"));
            }

            // Generate reset code
            String resetCode = generateVerificationCode();
            user.setPasswordResetCode(resetCode);
            user.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);

            logger.info("Generated password reset code for {}:  {}", email, resetCode);

            // Send reset email
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullname(), resetCode);

            logger.info("Password reset email sent to: {}", user.getEmail());

            return ResponseEntity.ok(Map.of("message", "Reset-Code wurde an Ihre E-Mail gesendet"));

        } catch (Exception ex) {
            logger. error("Password reset request failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Fehler beim Senden des Reset-Codes"));
        }
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");

            if (email == null || code == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "E-Mail und Code sind erforderlich"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
            if (userOpt.isEmpty()) {
                return ResponseEntity. badRequest().body(Map.of("error", "Ungültiger Code"));
            }

            User user = userOpt.get();

            if (!code.equals(user.getPasswordResetCode())) {
                logger.warn("Invalid reset code attempt for:  {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Ungültiger Code"));
            }

            if (user.getPasswordResetExpiry() == null || user.getPasswordResetExpiry().isBefore(LocalDateTime. now())) {
                logger. warn("Expired reset code attempt for: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Code ist abgelaufen"));
            }

            // Generate reset token for next step
            String resetToken = generateResetToken();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(15)); // 15 minutes for password change
            userRepository.save(user);

            logger.info("Reset code verified for: {}", email);

            return ResponseEntity.ok(Map.of(
                    "message", "Code verifiziert",
                    "resetToken", resetToken
            ));

        } catch (Exception ex) {
            logger. error("Reset code verification failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map. of("error", "Verifizierung fehlgeschlagen"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String resetToken = request.get("resetToken");
            String newPassword = request.get("newPassword");

            if (email == null || resetToken == null || newPassword == null) {
                return ResponseEntity. badRequest().body(Map.of("error", "Alle Felder sind erforderlich"));
            }

            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest().body(Map.of("error", "Passwort muss mindestens 8 Zeichen haben"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ungültiger Reset-Token"));
            }

            User user = userOpt.get();

            if (!resetToken. equals(user.getPasswordResetToken())) {
                logger. warn("Invalid reset token attempt for:  {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Ungültiger Reset-Token"));
            }

            if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
                logger.warn("Expired reset token attempt for:  {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Reset-Token ist abgelaufen"));
            }

            // Update password
            user.setPassword_hash(passwordEncoder.encode(newPassword));
            user.setPasswordResetCode(null);
            user.setPasswordResetExpiry(null);
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);
            userRepository.save(user);

            logger.info("Password successfully reset for: {}", email);

            return ResponseEntity.ok(Map.of("message", "Passwort erfolgreich geändert"));

        } catch (Exception ex) {
            logger.error("Password reset failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map. of("error", "Passwort konnte nicht geändert werden"));
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private String generateResetToken() {
        Random random = new Random();
        return String.format("%016x", random.nextLong()) + String.format("%016x", random.nextLong());
    }
}