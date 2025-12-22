package org.nikolic.programm.services;

import org.nikolic. programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org. nikolic.programm.repositories. UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util. HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

@Service
public class RegistrationCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationCacheService.class);

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Verbessertes Email-Pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Simplified cache
    private final Map<String, RegistrationData> registrationCache = new HashMap<>();

    public RegistrationCacheService(EmailService emailService,
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Inner class für Registration Data
    public static class RegistrationData {
        private String fullname;
        private String email;
        private String passwordHash;
        private String verificationCode;
        private LocalDateTime expiry;

        public RegistrationData(String fullname, String email, String passwordHash, String verificationCode, LocalDateTime expiry) {
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

    /**
     * ✅ Hauptmethode - Erstellt Registrierung und sendet Code
     */
    public void createRegistrationAndSendCode(String fullname, String email, String password) {
        logger.info("🚀 Starting registration for: {}", email);

        // ✅ Verbesserte Validation
        if (fullname == null || fullname.trim().isEmpty()) {
            logger.warn("❌ Validation failed: fullname empty");
            throw new IllegalArgumentException("Name ist erforderlich");
        }

        if (email == null || email.trim().isEmpty()) {
            logger.warn("❌ Validation failed: email empty");
            throw new IllegalArgumentException("E-Mail-Adresse ist erforderlich");
        }

        // ✅ Verbesserte E-Mail-Validierung
        if (!isValidEmail(email)) {
            logger.warn("❌ Validation failed: invalid email format:  {}", email);
            throw new IllegalArgumentException("Gültige E-Mail-Adresse ist erforderlich");
        }

        if (password == null || password.length() < 8) {
            logger.warn("❌ Validation failed: password too short");
            throw new IllegalArgumentException("Passwort muss mindestens 8 Zeichen haben");
        }

        String cleanEmail = email.toLowerCase().trim();
        logger.info("📝 Registration data validated for: {}", cleanEmail);

        // Check if user already exists
        if (userRepository.existsByEmail(cleanEmail)) {
            logger.warn("❌ Email already registered: {}", cleanEmail);
            throw new IllegalArgumentException("E-Mail-Adresse bereits registriert");
        }

        // Generate verification code
        String verificationCode = generateVerificationCode();

        // Hash password
        String passwordHash = passwordEncoder.encode(password);

        // Store in cache
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        RegistrationData data = new RegistrationData(
                fullname. trim(),
                cleanEmail,
                passwordHash,
                verificationCode,
                expiry
        );

        registrationCache.put(cleanEmail, data);

        logger.info("💾 Registration cached for {}: code {}", cleanEmail, verificationCode);

        // ✅ Send verification email
        try {
            emailService.sendVerificationEmail(cleanEmail, fullname. trim(), verificationCode);
            logger.info("📧 Verification email sent successfully to: {}", cleanEmail);
        } catch (Exception e) {
            // Remove from cache if email sending fails
            registrationCache.remove(cleanEmail);
            logger. error("❌ Failed to send verification email to: {}", cleanEmail, e);
            throw new RuntimeException("E-Mail konnte nicht gesendet werden:  " + e.getMessage());
        }
    }

    /**
     * ✅ Verifiziert Code und erstellt User
     */
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
            logger. warn("Invalid verification code for: {}", cleanEmail);
            return null;
        }

        try {
            // Create user in database
            User user = new User();
            UserRole role = UserRole.USER; // Default role assignment
            user.setEmail(data.getEmail());
            user.setFullname(data.getFullname());
            user.setPassword_hash(data.getPasswordHash());
            user.setRole(role);
            user.setIs_active(true); // Activate immediately after verification
            user.setEmailVerified(true); // @Transient field
            user.setCreated_at(LocalDateTime.now());

            User savedUser = userRepository.save(user);

            // Remove from cache after successful creation
            registrationCache.remove(cleanEmail);

            logger.info("User created successfully:  {}", savedUser.getEmail());

            return savedUser;

        } catch (Exception e) {
            logger.error("Failed to create user for: {}", cleanEmail, e);
            throw new RuntimeException("Benutzer konnte nicht erstellt werden: " + e.getMessage());
        }
    }

    /**
     * ✅ Sendet Code erneut
     */
    public void resendCode(String email) {
        String cleanEmail = email.toLowerCase().trim();
        RegistrationData data = registrationCache.get(cleanEmail);

        if (data == null) {
            throw new IllegalArgumentException("Keine ausstehende Registrierung für diese E-Mail gefunden");
        }

        // Generate new code
        String newCode = generateVerificationCode();

        // Update cache with new code and extended expiry
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

        // Send new verification email
        try {
            emailService.sendVerificationEmail(cleanEmail, data.getFullname(), newCode);
            logger.info("New verification email sent to: {}", cleanEmail);
        } catch (Exception e) {
            logger.error("Failed to resend verification email to: {}", cleanEmail, e);
            throw new RuntimeException("E-Mail konnte nicht gesendet werden: " + e.getMessage());
        }
    }

    public boolean hasVerificationPending(String email) {
        RegistrationData data = registrationCache.get(email. toLowerCase().trim());
        return data != null && ! data.isExpired();
    }

    // ✅ Verbesserte Email-Validierung
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.debug("Email validation failed: null or empty");
            return false;
        }

        String trimmedEmail = email.trim();

        // Basic checks
        if (! trimmedEmail.contains("@")) {
            logger.debug("Email validation failed: no @ symbol");
            return false;
        }

        if (! trimmedEmail.contains(".")) {
            logger.debug("Email validation failed: no domain");
            return false;
        }

        // Length check
        if (trimmedEmail.length() < 5 || trimmedEmail. length() > 254) {
            logger.debug("Email validation failed: invalid length");
            return false;
        }

        // Regex pattern check
        boolean matches = EMAIL_PATTERN.matcher(trimmedEmail).matches();
        logger.debug("Email validation for '{}': {}", trimmedEmail, matches ?  "PASSED" : "FAILED");

        return matches;
    }

    // Helper Methods
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Get registration statistics
     */
    public RegistrationStatistics getStatistics() {
        int pendingRegistrations = registrationCache.size();
        long expiredCount = registrationCache.values().stream()
                .mapToLong(data -> data.isExpired() ? 1 : 0)
                .sum();

        return new RegistrationStatistics(pendingRegistrations, expiredCount);
    }

    // Statistics DTO
    public static class RegistrationStatistics {
        private final int pendingRegistrations;
        private final long expiredRegistrations;

        public RegistrationStatistics(int pendingRegistrations, long expiredRegistrations) {
            this.pendingRegistrations = pendingRegistrations;
            this.expiredRegistrations = expiredRegistrations;
        }

        public int getPendingRegistrations() { return pendingRegistrations; }
        public long getExpiredRegistrations() { return expiredRegistrations; }
    }
}