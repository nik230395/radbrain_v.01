package org.nikolic.programm.services;

import org.nikolic.programm.entities.EmailVerification;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * RegistrationCacheService - Verbessert mit Cleanup
 *
 * Änderungen:
 * - Scheduled Cleanup gegen Memory Leaks
 * - Thread-safe ConcurrentHashMap
 * - Bessere Validation
 * - EmailVerification Entity Support
 */
@Service
public class RegistrationCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationCacheService.class);

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Thread-safe Cache (ConcurrentHashMap statt HashMap)
    private final Map<String, RegistrationData> registrationCache = new ConcurrentHashMap<>();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public RegistrationCacheService(EmailService emailService,
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Scheduled Cleanup - Läuft alle 5 Minuten
     * Verhindert Memory Leaks durch expired entries
     */
    @Scheduled(fixedRate = 300000) // 5 Minuten
    public void cleanupExpiredRegistrations() {
        logger.debug("Running scheduled cleanup of expired registrations");

        int sizeBefore = registrationCache.size();

        registrationCache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                logger.debug("Removing expired registration for: {}", entry.getKey());
            }
            return expired;
        });

        int sizeAfter = registrationCache.size();
        int removed = sizeBefore - sizeAfter;

        if (removed > 0) {
            logger.info("Cleaned up {} expired registrations. Remaining: {}", removed, sizeAfter);
        }
    }

    /**
     * Hauptmethode - Erstellt Registrierung und sendet Code
     */
    public void createRegistrationAndSendCode(String fullname, String email, String password) {
        logger.info("Starting registration for: {}", email);

        // Validation
        validateRegistrationData(fullname, email, password);

        String cleanEmail = email.toLowerCase().trim();

        // Prüfe ob User bereits existiert
        if (userRepository.existsByEmail(cleanEmail)) {
            logger.warn("Registration attempt for existing email: {}", cleanEmail);
            throw new IllegalArgumentException("E-Mail-Adresse bereits registriert");
        }

        // Generiere Verification Code
        String verificationCode = generateVerificationCode();

        // Hash Password
        String passwordHash = passwordEncoder.encode(password);

        // Speichere in Cache
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        RegistrationData data = new RegistrationData(
                fullname.trim(),
                cleanEmail,
                passwordHash,
                verificationCode,
                expiry
        );

        registrationCache.put(cleanEmail, data);

        logger.info("Registration cached for {}: code {}", cleanEmail, verificationCode);

        // Sende Verification Email
        try {
            emailService.sendVerificationEmail(cleanEmail, fullname.trim(), verificationCode);
            logger.info("Verification email sent successfully to: {}", cleanEmail);
        } catch (Exception e) {
            // Remove from cache if email sending fails
            registrationCache.remove(cleanEmail);
            logger.error("Failed to send verification email to: {}", cleanEmail, e);
            throw new RuntimeException("E-Mail konnte nicht gesendet werden: " + e.getMessage());
        }
    }

    /**
     * Verifiziert Code und erstellt User mit EmailVerification Entity
     */
    @Transactional
    public User verifyAndCreateUser(String email, String code) {
        String cleanEmail = email.toLowerCase().trim();
        RegistrationData data = registrationCache.get(cleanEmail);

        if (data == null) {
            logger.warn("No registration found for: {}", cleanEmail);
            return null;
        }

        if (data.isExpired()) {
            registrationCache.remove(cleanEmail);
            logger.warn("Expired registration attempt for: {}", cleanEmail);
            return null;
        }

        if (!code.equals(data.getVerificationCode())) {
            logger.warn("Invalid verification code for: {}", cleanEmail);
            return null;
        }

        try {
            // Erstelle User
            User user = new User();
            user.setEmail(data.getEmail());
            user.setFullname(data.getFullname());
            user.setPasswordHash(data.getPasswordHash());
            user.setRole(UserRole.USER);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());

            // Erstelle EmailVerification Entity
            EmailVerification verification = new EmailVerification();
            verification.setUser(user);
            verification.setVerificationCode(code);
            verification.setExpiresAt(data.getExpiry());
            verification.setVerified(true);
            verification.setVerifiedAt(LocalDateTime.now());
            verification.setCreatedAt(LocalDateTime.now());

            // Setze Relationship
            user.setEmailVerification(verification);

            // Speichere User (cascadiert zu EmailVerification)
            User savedUser = userRepository.save(user);

            // Remove from cache after successful creation
            registrationCache.remove(cleanEmail);

            logger.info("User created successfully: {}", savedUser.getEmail());

            // Sende Welcome Email (non-blocking)
            try {
                emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullname());
            } catch (Exception e) {
                logger.warn("Failed to send welcome email: {}", e.getMessage());
                // Don't fail user creation if welcome email fails
            }

            return savedUser;

        } catch (Exception e) {
            logger.error("Failed to create user for: {}", cleanEmail, e);
            throw new RuntimeException("Benutzer konnte nicht erstellt werden: " + e.getMessage());
        }
    }

    /**
     * Sendet Code erneut
     */
    public void resendCode(String email) {
        String cleanEmail = email.toLowerCase().trim();
        RegistrationData data = registrationCache.get(cleanEmail);

        if (data == null) {
            throw new IllegalArgumentException("Keine ausstehende Registrierung für diese E-Mail gefunden");
        }

        // Generiere neuen Code
        String newCode = generateVerificationCode();

        // Update Cache mit neuem Code und verlängerter Expiry
        LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(10);
        RegistrationData newData = new RegistrationData(
                data.getFullname(),
                data.getEmail(),
                data.getPasswordHash(),
                newCode,
                newExpiry
        );

        registrationCache.put(cleanEmail, newData);

        logger.info("New verification code generated for {}: {}", cleanEmail, newCode);

        // Sende neue Verification Email
        try {
            emailService.sendVerificationEmail(cleanEmail, data.getFullname(), newCode);
            logger.info("New verification email sent to: {}", cleanEmail);
        } catch (Exception e) {
            logger.error("Failed to resend verification email to: {}", cleanEmail, e);
            throw new RuntimeException("E-Mail konnte nicht gesendet werden: " + e.getMessage());
        }
    }

    /**
     * Prüft ob Verification pending ist
     */
    public boolean hasVerificationPending(String email) {
        RegistrationData data = registrationCache.get(email.toLowerCase().trim());
        return data != null && !data.isExpired();
    }

    /**
     * Holt Statistiken
     */
    public RegistrationStatistics getStatistics() {
        int total = registrationCache.size();
        long expired = registrationCache.values().stream()
                .filter(RegistrationData::isExpired)
                .count();
        int active = total - (int) expired;

        return new RegistrationStatistics(total, active, (int) expired);
    }

    /**
     * Manuelle Cleanup-Methode (kann von Admin aufgerufen werden)
     */
    public void manualCleanup() {
        logger.info("Manual cleanup triggered");
        cleanupExpiredRegistrations();
    }

    // Private Helper Methods

    private void validateRegistrationData(String fullname, String email, String password) {
        if (fullname == null || fullname.trim().isEmpty()) {
            throw new IllegalArgumentException("Name ist erforderlich");
        }

        if (fullname.trim().length() < 2) {
            throw new IllegalArgumentException("Name muss mindestens 2 Zeichen haben");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-Mail-Adresse ist erforderlich");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Gültige E-Mail-Adresse ist erforderlich");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Passwort muss mindestens 8 Zeichen haben");
        }

        // Optional: Password Complexity Check
        if (!hasPasswordComplexity(password)) {
            throw new IllegalArgumentException("Passwort muss Buchstaben und Zahlen enthalten");
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String trimmedEmail = email.trim();

        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return false;
        }

        if (trimmedEmail.length() < 5 || trimmedEmail.length() > 254) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmedEmail).matches();
    }

    private boolean hasPasswordComplexity(String password) {
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    // Inner Classes

    public static class RegistrationData {
        private final String fullname;
        private final String email;
        private final String passwordHash;
        private final String verificationCode;
        private final LocalDateTime expiry;

        public RegistrationData(String fullname, String email, String passwordHash,
                                String verificationCode, LocalDateTime expiry) {
            this.fullname = fullname;
            this.email = email;
            this.passwordHash = passwordHash;
            this.verificationCode = verificationCode;
            this.expiry = expiry;
        }

        public String getFullname() { return fullname; }
        public String getEmail() { return email; }
        public String getPasswordHash() { return passwordHash; }
        public String getVerificationCode() { return verificationCode; }
        public LocalDateTime getExpiry() { return expiry; }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }

    public static class RegistrationStatistics {
        private final int totalRegistrations;
        private final int activeRegistrations;
        private final int expiredRegistrations;

        public RegistrationStatistics(int total, int active, int expired) {
            this.totalRegistrations = total;
            this.activeRegistrations = active;
            this.expiredRegistrations = expired;
        }

        public int getTotalRegistrations() { return totalRegistrations; }
        public int getActiveRegistrations() { return activeRegistrations; }
        public int getExpiredRegistrations() { return expiredRegistrations; }
    }
}