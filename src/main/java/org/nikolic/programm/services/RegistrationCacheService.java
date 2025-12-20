package org.nikolic.programm.services;

import org.nikolic.programm.dtos.RegistrationCacheEntry;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.Role;
import org.nikolic.programm.repositories.RoleRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.Optional;

@Service
public class RegistrationCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationCacheService.class);

    private final Cache registrations; // Spring Cache
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;

    public RegistrationCacheService(CacheManager cacheManager,
                                    EmailService emailService,
                                    PasswordEncoder passwordEncoder,
                                    UserRepository userRepository,
                                    RoleRepository roleRepository) {
        this.registrations = cacheManager.getCache("registrations");
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            try (Formatter fmt = new Formatter()) {
                for (byte b : digest) fmt.format("%02x", b);
                return fmt.toString();
            }
        } catch (Exception ex) {
            throw new RuntimeException("hashing failed", ex);
        }
    }

    private String generateNumericCode(int digits) {
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;
        int code = secureRandom.nextInt(max - min + 1) + min;
        return Integer.toString(code);
    }

    /**
     * Create a registration cache entry and send verification email.
     * Throws MailException if sending fails (controller should handle).
     */
    public void createRegistrationAndSendCode(String fullname, String email, String rawPassword) throws MailException {
        // Prevent duplicate registrations if email already exists in DB
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email bereits registriert");
        }

        // Prepare entry
        RegistrationCacheEntry entry = new RegistrationCacheEntry();
        entry.setEmail(email);
        entry.setFullname(fullname);
        entry.setPasswordHash(passwordEncoder.encode(rawPassword)); // store hashed password
        entry.setCreatedAt(LocalDateTime.now());
        entry.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));

        // generate code and hash it
        String code = generateNumericCode(CODE_LENGTH);
        entry.setCodeHash(sha256Hex(code));

        // store in cache using email as key
        registrations.put(email.toLowerCase(), entry);

        // send plain code by email
        emailService.sendVerificationEmail(email, code);

        // don't log code in prod
        logger.info("Registration created for {} (expires at {})", email, entry.getExpiresAt());
    }

    /**
     * Verify code and create User on success. Returns created User.
     */
    public User verifyAndCreateUser(String email, String plainCode) {
        Cache.ValueWrapper wrapper = registrations.get(email.toLowerCase());
        if (wrapper == null) return null;
        RegistrationCacheEntry entry = (RegistrationCacheEntry) wrapper.get();

        // expired check
        if (entry.getExpiresAt() != null && entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            registrations.evict(email.toLowerCase());
            return null;
        }

        String providedHash = sha256Hex(plainCode);
        if (!providedHash.equals(entry.getCodeHash())) {
            return null;
        }

        // create user entity and save
        User u = new User();
        u.setEmail(entry.getEmail());
        u.setPassword_hash(entry.getPasswordHash()); // hashed already
        u.setFullname(entry.getFullname());
        u.setIs_active(true); // verified
        u.setCreated_at(LocalDateTime.now());

        // assign role if exists
        try {
            Optional<Role> r = roleRepository.findByName("ROLE_USER");
            if (r.isPresent()) u.addRole(r.get()); else u.setRole("user");
        } catch (Exception ex) {
            u.setRole("user");
        }

        User saved = userRepository.save(u);

        // remove from cache
        registrations.evict(email.toLowerCase());

        return saved;
    }

    /**
     * Optional: allow resending — generate new code, update cache and re-send email.
     */
    public void resendCode(String email) throws MailException {
        Cache.ValueWrapper wrapper = registrations.get(email.toLowerCase());
        if (wrapper == null) {
            throw new IllegalArgumentException("No pending registration for email");
        }
        RegistrationCacheEntry entry = (RegistrationCacheEntry) wrapper.get();
        String code = generateNumericCode(CODE_LENGTH);
        entry.setCodeHash(sha256Hex(code));
        entry.setCreatedAt(LocalDateTime.now());
        entry.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        registrations.put(email.toLowerCase(), entry);
        emailService.sendVerificationEmail(email, code);
    }
}