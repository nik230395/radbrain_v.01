package org.nikolic.programm.services;

import org.nikolic.programm.entities.PasswordReset;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.PasswordResetRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * PasswordResetService - Optional
 *
 * Zentralisiert Password-Reset-Logik
 * Kann von Controller verwendet werden für sauberere Trennung
 */
@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final PasswordResetRepository passwordResetRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetRepository passwordResetRepository,
                                UserRepository userRepository,
                                EmailService emailService,
                                PasswordEncoder passwordEncoder) {
        this.passwordResetRepository = passwordResetRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Erstellt Password-Reset und sendet Email
     */
    @Transactional
    public PasswordResetResult initiateReset(String email, String ipAddress) {
        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());

        if (userOpt.isEmpty()) {
            logger.info("Password reset requested for non-existent email: {}", email);
            return PasswordResetResult.success("Code wurde gesendet");
        }

        User user = userOpt.get();

        if (!user.isEmailVerified() || !user.isActive()) {
            return PasswordResetResult.failed("Konto ist nicht aktiviert");
        }

        // Invalidiere alte Resets
        passwordResetRepository.markAllAsUsedForUser(user, LocalDateTime.now());

        // Erstelle neuen Reset
        String resetCode = generateVerificationCode();
        PasswordReset passwordReset = new PasswordReset(user, resetCode, 10);
        passwordReset.setIpAddress(ipAddress);
        passwordResetRepository.save(passwordReset);

        logger.info("Created password reset for: {} (IP: {})", email, ipAddress);

        // Sende Email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullname(), resetCode);
            return PasswordResetResult.success("Reset-Code wurde gesendet");
        } catch (Exception e) {
            logger.error("Failed to send reset email", e);
            return PasswordResetResult.failed("E-Mail konnte nicht gesendet werden");
        }
    }

    /**
     * Verifiziert Reset-Code und generiert Token
     */
    @Transactional
    public PasswordResetResult verifyCode(String email, String code) {
        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());

        if (userOpt.isEmpty()) {
            return PasswordResetResult.failed("Ungültiger Code");
        }

        User user = userOpt.get();
        Optional<PasswordReset> resetOpt = passwordResetRepository.findActiveByUser(user);

        if (resetOpt.isEmpty()) {
            return PasswordResetResult.failed("Kein aktiver Reset-Code gefunden");
        }

        PasswordReset passwordReset = resetOpt.get();

        if (passwordReset.isExpired()) {
            return PasswordResetResult.failed("Code ist abgelaufen");
        }

        if (passwordReset.hasExceededMaxAttempts()) {
            passwordReset.markAsUsed();
            passwordResetRepository.save(passwordReset);
            return PasswordResetResult.failed("Zu viele Versuche");
        }

        if (!code.equals(passwordReset.getResetCode())) {
            passwordReset.incrementAttempts();
            passwordResetRepository.save(passwordReset);
            return PasswordResetResult.failed("Ungültiger Code");
        }

        // Generiere Token
        String resetToken = generateResetToken();
        passwordReset.generateToken(resetToken, 15);
        passwordResetRepository.save(passwordReset);

        logger.info("Reset code verified for: {}", email);

        return PasswordResetResult.success("Code verifiziert", resetToken);
    }

    /**
     * Setzt neues Passwort mit Token
     */
    @Transactional
    public PasswordResetResult resetPassword(String email, String token, String newPassword) {
        if (newPassword.length() < 8) {
            return PasswordResetResult.failed("Passwort muss mindestens 8 Zeichen haben");
        }

        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());

        if (userOpt.isEmpty()) {
            return PasswordResetResult.failed("Ungültiger Token");
        }

        User user = userOpt.get();

        Optional<PasswordReset> resetOpt = passwordResetRepository
                .findByValidToken(token, LocalDateTime.now());

        if (resetOpt.isEmpty()) {
            return PasswordResetResult.failed("Ungültiger oder abgelaufener Token");
        }

        PasswordReset passwordReset = resetOpt.get();

        if (!passwordReset.getUser().getId().equals(user.getId())) {
            logger.error("Token user mismatch for: {}", email);
            return PasswordResetResult.failed("Ungültiger Token");
        }

        // Setze Passwort
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Markiere als verwendet
        passwordReset.markAsUsed();
        passwordResetRepository.save(passwordReset);

        // Invalidiere alle anderen
        passwordResetRepository.markAllAsUsedForUser(user, LocalDateTime.now());

        logger.info("Password successfully reset for: {}", email);

        return PasswordResetResult.success("Passwort erfolgreich geändert");
    }

    /**
     * Scheduled Cleanup - Läuft täglich um 3 Uhr
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void scheduledCleanup() {
        logger.info("Running scheduled password reset cleanup");

        LocalDateTime usedCutoff = LocalDateTime.now().minusDays(7);
        LocalDateTime expiredCutoff = LocalDateTime.now().minusDays(1);

        passwordResetRepository.deleteOldUsedResets(usedCutoff);
        passwordResetRepository.deleteExpiredResets(expiredCutoff);

        logger.info("Password reset cleanup completed");
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

    // Result DTO

    public static class PasswordResetResult {
        private final boolean success;
        private final String message;
        private final String token;

        private PasswordResetResult(boolean success, String message, String token) {
            this.success = success;
            this.message = message;
            this.token = token;
        }

        public static PasswordResetResult success(String message) {
            return new PasswordResetResult(true, message, null);
        }

        public static PasswordResetResult success(String message, String token) {
            return new PasswordResetResult(true, message, token);
        }

        public static PasswordResetResult failed(String message) {
            return new PasswordResetResult(false, message, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getToken() { return token; }
    }
}