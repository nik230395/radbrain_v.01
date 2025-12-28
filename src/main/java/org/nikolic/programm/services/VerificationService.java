package org.nikolic.programm.services;

import org.nikolic.programm.entities.EmailVerification;
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

/**
 * VerificationService - Angepasst an EmailVerification Entity
 *
 * ACHTUNG: Diese Klasse ist jetzt OPTIONAL, da die Logik
 * bereits in RegisterController implementiert ist!
 *
 * Du kannst sie entweder:
 * 1. Löschen (wenn nicht verwendet)
 * 2. Oder für zusätzliche Helper-Methoden nutzen
 */
@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private final EmailService emailService;
    private final UserRepository userRepository;

    // Cache für Verification Data (falls User noch nicht in DB ist)
    private final Map<String, VerificationData> verificationCache = new HashMap<>();

    public VerificationService(EmailService emailService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    /**
     * Erstellt Verification Code für User der schon in DB ist
     */
    public String createVerificationCodeForUser(User user) {
        String code = generateVerificationCode();

        // ✅ ANGEPASST: Verwende EmailVerification Entity
        EmailVerification verification = user.getEmailVerification();

        if (verification == null) {
            verification = new EmailVerification(user, code, 10);
            user.setEmailVerification(verification);
        } else {
            verification.regenerateCode(code, 10);
        }

        userRepository.save(user);

        logger.info("Created verification code for user {}: {}", user.getEmail(), code);
        return code;
    }

    /**
     * Erstellt Verification Code und speichert in Cache (für Registrierung)
     */
    public String createVerificationCode(String email, String fullname) {
        String code = generateVerificationCode();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        verificationCache.put(email, new VerificationData(code, expiry, fullname, email));

        logger.info("Created verification code for {}: {}", email, code);
        return code;
    }

    /**
     * Sendet Verification Email
     */
    public void sendVerificationEmail(String email, String fullname, String code) {
        try {
            emailService.sendVerificationEmail(email, fullname, code);
            logger.info("✅ Verification email sent to: {}", email);
        } catch (Exception e) {
            logger.error("❌ Failed to send verification email to: {}", email, e);
            throw new RuntimeException("E-Mail konnte nicht gesendet werden", e);
        }
    }

    /**
     * Sendet Verification Email (lädt Code aus User)
     */
    public void sendVerificationEmail(String email, String fullname) {
        try {
            // Schaue im Cache nach
            VerificationData data = verificationCache.get(email);
            if (data != null && !data.isExpired()) {
                emailService.sendVerificationEmail(email, fullname, data.getCode());
                logger.info("Verification email sent to: {} from cache", email);
                return;
            }

            // ✅ ANGEPASST: Schaue in Database
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                EmailVerification verification = user.getEmailVerification();

                if (verification != null && verification.isValid()) {
                    emailService.sendVerificationEmail(email, fullname, verification.getVerificationCode());
                    logger.info("Verification email sent to: {} from database", email);
                    return;
                }
            }

            throw new RuntimeException("Keine Verifikation gefunden für: " + email);

        } catch (Exception e) {
            logger.error("❌ Failed to send verification email to: {}", email, e);
            throw new RuntimeException("E-Mail konnte nicht gesendet werden", e);
        }
    }

    /**
     * Versendet Email erneut
     */
    public void resendVerificationEmail(String email) {
        // Cache prüfen
        VerificationData data = verificationCache.get(email);
        if (data != null) {
            sendVerificationEmail(data.getEmail(), data.getFullname(), data.getCode());
            return;
        }

        // ✅ ANGEPASST: Database prüfen
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Keine ausstehende Verifikation für diese E-Mail gefunden");
        }

        User user = userOpt.get();
        EmailVerification verification = user.getEmailVerification();

        if (verification == null || verification.isVerified()) {
            throw new RuntimeException("Keine ausstehende Verifikation für diese E-Mail gefunden");
        }

        // Generiere neuen Code
        String newCode = generateVerificationCode();
        verification.regenerateCode(newCode, 10);
        userRepository.save(user);

        sendVerificationEmail(user.getEmail(), user.getFullname(), newCode);
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
                verificationCache.remove(email);
            }
            return isValid;
        }

        // ✅ ANGEPASST: Prüfe Database
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            EmailVerification verification = user.getEmailVerification();

            if (verification != null && verification.isValid()) {
                return code.equals(verification.getVerificationCode());
            }
        }

        return false;
    }

    /**
     * Erstellt und versendet neuen Code
     */
    public void createAndSendVerificationCode(String email, String fullname) {
        String code = createVerificationCode(email, fullname);
        emailService.sendVerificationEmail(email, fullname, code);
    }

    /**
     * Prüft ob Verifikation anhängig ist
     */
    public boolean hasVerificationPending(String email) {
        // Cache prüfen
        VerificationData data = verificationCache.get(email);
        if (data != null && !data.isExpired()) {
            return true;
        }

        // ✅ ANGEPASST: Database prüfen
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            EmailVerification verification = user.getEmailVerification();
            return verification != null && !verification.isVerified() && verification.isValid();
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
        VerificationData data = verificationCache.get(email);
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

    /**
     * Markiert Email als verifiziert
     */
    public void markEmailAsVerified(String email) {
        verificationCache.remove(email);
        logger.info("Marked email as verified and removed from cache: {}", email);
    }

    // Helper Methods

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    // Inner Class für Cache
    public static class VerificationData {
        private final String code;
        private final LocalDateTime expiry;
        private final String fullname;
        private final String email;

        public VerificationData(String code, LocalDateTime expiry, String fullname, String email) {
            this.code = code;
            this.expiry = expiry;
            this.fullname = fullname;
            this.email = email;
        }

        public String getCode() { return code; }
        public LocalDateTime getExpiry() { return expiry; }
        public String getFullname() { return fullname; }
        public String getEmail() { return email; }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }
}