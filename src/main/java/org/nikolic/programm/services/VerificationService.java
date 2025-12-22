package org.nikolic.programm.services;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Optional;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private final EmailService emailService;
    private final UserRepository userRepository;

    // Cache für Verification Data (alternativ zu Database)
    private final Map<String, VerificationData> verificationCache = new HashMap<>();

    public VerificationService(EmailService emailService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    // Inner class für Verification Data
    public static class VerificationData {
        private String code;
        private LocalDateTime expiry;
        private String fullname;
        private String email;

        public VerificationData(String code, LocalDateTime expiry, String fullname, String email) {
            this.code = code;
            this.expiry = expiry;
            this.fullname = fullname;
            this.email = email;
        }

        // Getters
        public String getCode() { return code; }
        public LocalDateTime getExpiry() { return expiry; }
        public String getFullname() { return fullname; }
        public String getEmail() { return email; }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }

    /**
     * Erstellt Verification Code und speichert ihn
     */
    public String createVerificationCode(String email, String fullname) {
        String code = generateVerificationCode();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        verificationCache.put(email, new VerificationData(code, expiry, fullname, email));

        logger.info("Created verification code for {}: {}", email, code);
        return code;
    }

    /**
     * Sendet Verification Email mit korrekten 3 Parametern
     */
    public void sendVerificationEmail(String email, String fullname) {
        try {
            // Schaue zuerst im Cache nach
            VerificationData data = verificationCache.get(email);
            if (data == null) {
                // Falls nicht im Cache, versuche aus Database
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getVerificationCode() != null && user.getVerificationCodeExpiry() != null) {
                        // ✅ Korrigierter Aufruf mit allen 3 Parametern
                        emailService.sendVerificationEmail(email, fullname, user.getVerificationCode());
                        logger.info("Verification email sent to: {} from database", email);
                        return;
                    }
                }
                throw new RuntimeException("Keine Verifikation gefunden für:  " + email);
            }

            if (data.isExpired()) {
                verificationCache.remove(email);
                throw new RuntimeException("Verifikationscode ist abgelaufen");
            }

            // ✅ Korrigierter Aufruf mit allen 3 Parametern (email, fullname, code)
            emailService.sendVerificationEmail(email, fullname, data.getCode());

            logger.info("✅ Verification email sent to:  {}", email);
        } catch (Exception e) {
            logger.error("❌ Failed to send verification email to: {}", email, e);
            throw new RuntimeException("E-Mail konnte nicht gesendet werden", e);
        }
    }

    /**
     * Versendet Email erneut
     */
    public void resendVerificationEmail(String email) {
        VerificationData data = verificationCache.get(email);
        if (data == null) {
            // Versuche aus Database zu laden
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("Keine ausstehende Verifikation für diese E-Mail gefunden");
            }

            User user = userOpt. get();
            if (user. getVerificationCode() == null || user.isEmailVerified()) {
                throw new RuntimeException("Keine ausstehende Verifikation für diese E-Mail gefunden");
            }

            // ✅ Verwende User-Daten
            sendVerificationEmail(user.getEmail(), user.getFullname());
        } else {
            // ✅ Verwende Cache-Daten
            sendVerificationEmail(data.getEmail(), data.getFullname());
        }
    }

    /**
     * Verifiziert Code
     */
    public boolean verifyCode(String email, String code) {
        // Prüfe Cache zuerst
        VerificationData data = verificationCache.get(email);
        if (data != null) {
            if (data.isExpired()) {
                verificationCache.remove(email);
                return false;
            }

            boolean isValid = code.equals(data.getCode());
            if (isValid) {
                verificationCache.remove(email); // Code nach Verwendung entfernen
            }
            return isValid;
        }

        // Prüfe Database
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getVerificationCode() != null &&
                    user.getVerificationCodeExpiry() != null &&
                    user.getVerificationCodeExpiry().isAfter(LocalDateTime.now())) {
                return code.equals(user.getVerificationCode());
            }
        }

        return false;
    }

    /**
     * Erstellt und versendet neuen Code
     */
    public void createAndSendVerificationCode(String email, String fullname) {
        String code = createVerificationCode(email, fullname);

        // ✅ Korrigierter Aufruf mit allen 3 Parametern
        emailService.sendVerificationEmail(email, fullname, code);
    }

    /**
     * Prüft ob Verifikation anhängig ist
     */
    public boolean hasVerificationPending(String email) {
        // Cache prüfen
        VerificationData data = verificationCache.get(email);
        if (data != null && ! data.isExpired()) {
            return true;
        }

        // Database prüfen
        Optional<User> userOpt = userRepository. findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return ! user.isEmailVerified() &&
                    user.getVerificationCode() != null &&
                    user.getVerificationCodeExpiry() != null &&
                    user.getVerificationCodeExpiry().isAfter(LocalDateTime. now());
        }

        return false;
    }

    /**
     * Räumt abgelaufene Einträge auf
     */
    public void cleanupExpiredEntries() {
        verificationCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        logger.info("Cleaned up expired verification entries");
    }

    /**
     * Holt Fullname aus Cache oder Database
     */
    public String getFullnameForEmail(String email) {
        // Cache prüfen
        VerificationData data = verificationCache. get(email);
        if (data != null) {
            return data.getFullname();
        }

        // Database prüfen
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get().getFullname();
        }

        return "User"; // Fallback
    }

    // Helper Methods
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Markiert Email als verifiziert (optional für Cache-Management)
     */
    public void markEmailAsVerified(String email) {
        verificationCache.remove(email);
        logger.info("Marked email as verified and removed from cache:  {}", email);
    }
}