package org.nikolic.programm.controllers;

import jakarta.validation.Valid;
import org.nikolic.programm.dtos.RegisterRequest;
import org.nikolic.programm.entities.EmailVerification;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.EmailService;
import org.nikolic.programm.services.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * ✅ FIXED RegisterController
 *
 * Changes:
 * - Added @Valid annotation for automatic validation
 * - Improved error handling using GlobalExceptionHandler
 * - Better logging
 * - Cleaner code structure
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Validated
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;

    public RegisterController(UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              EmailService emailService,
                              JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    /**
     * User registration with email verification
     * ✅ FIXED: Added @Valid annotation for automatic validation
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.info("Registration attempt for email: {}", request.getEmail());

            String cleanEmail = request.getEmail().toLowerCase().trim();

            // Check if user already exists
            if (userRepository.existsByEmail(cleanEmail)) {
                logger.warn("Registration attempt for existing email: {}", cleanEmail);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "E-Mail-Adresse bereits registriert"));
            }

            // Generate verification code
            String verificationCode = generateVerificationCode();
            logger.debug("Generated verification code for {}", cleanEmail);

            // Create user
            User user = new User();
            user.setEmail(cleanEmail);
            user.setFullname(request.getFullname().trim());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.USER);
            user.setActive(false); // Not active until email verified
            user.setCreatedAt(LocalDateTime.now());

            // Create email verification entity
            EmailVerification emailVerification = new EmailVerification(
                    user,
                    verificationCode,
                    10 // 10 minutes validity
            );
            user.setEmailVerification(emailVerification);

            // Save user
            User savedUser = userRepository.save(user);
            logger.info("Created inactive user with ID: {}", savedUser.getId());

            // Send verification email
            try {
                emailService.sendVerificationEmail(
                        savedUser.getEmail(),
                        savedUser.getFullname(),
                        verificationCode
                );

                logger.info("Verification email sent successfully to: {}", savedUser.getEmail());

                return ResponseEntity.ok(Map.of(
                        "message", "Registrierung erfolgreich. Bestätigungscode wurde per E-Mail gesendet.",
                        "requiresVerification", true,
                        "email", savedUser.getEmail()
                ));

            } catch (Exception emailError) {
                // If email sending fails, delete the user to keep database clean
                userRepository.delete(savedUser);
                logger.error("Failed to send verification email to: {}", savedUser.getEmail(), emailError);

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "E-Mail konnte nicht gesendet werden. Bitte versuchen Sie es später erneut."));
            }

        } catch (Exception ex) {
            logger.error("Registration failed for email: {}", request.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registrierung fehlgeschlagen: " + ex.getMessage()));
        }
    }

    /**
     * Email verification with code
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");

            if (email == null || code == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "E-Mail und Code sind erforderlich"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
            if (userOpt.isEmpty()) {
                logger.warn("Verification attempt for non-existent user: {}", email);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Benutzer nicht gefunden"));
            }

            User user = userOpt.get();
            EmailVerification verification = user.getEmailVerification();

            if (verification == null) {
                logger.warn("No verification data for user: {}", email);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Keine Verifizierung ausstehend"));
            }

            // Check if already verified
            if (verification.isVerified()) {
                logger.warn("Verification attempt for already verified user: {}", email);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "E-Mail bereits verifiziert"));
            }

            // Check verification code
            if (!code.equals(verification.getVerificationCode())) {
                verification.incrementAttempts();
                userRepository.save(user);

                logger.warn("Invalid verification code for user: {}", email);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ungültiger Verifizierungscode"));
            }

            // Check if code is expired
            if (verification.isExpired()) {
                logger.warn("Expired verification code for user: {}", email);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Verifizierungscode ist abgelaufen"));
            }

            // Mark as verified and activate user
            verification.markAsVerified();
            user.setActive(true);
            userRepository.save(user);

            logger.info("User successfully verified and activated: {}", user.getEmail());

            // Send welcome email (non-blocking)
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getFullname());
            } catch (Exception e) {
                logger.warn("Failed to send welcome email to: {}", user.getEmail(), e);
            }

            // Generate JWT Token
            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "message", "E-Mail erfolgreich verifiziert",
                    "token", token,
                    "email", user.getEmail(),
                    "fullname", user.getFullname(),
                    "role", user.getRoleString()
            ));

        } catch (Exception ex) {
            logger.error("Email verification failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Verifizierung fehlgeschlagen: " + ex.getMessage()));
        }
    }

    /**
     * Resend verification code
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "E-Mail ist erforderlich"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Benutzer nicht gefunden"));
            }

            User user = userOpt.get();
            EmailVerification verification = user.getEmailVerification();

            if (verification == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Keine Verifizierung ausstehend"));
            }

            if (verification.isVerified()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "E-Mail bereits verifiziert"));
            }

            // Generate new verification code
            String newCode = generateVerificationCode();
            verification.regenerateCode(newCode, 10);
            userRepository.save(user);

            logger.info("New verification code generated for {}", email);

            // Send new verification email
            emailService.sendVerificationEmail(user.getEmail(), user.getFullname(), newCode);

            logger.info("Resent verification email to: {}", user.getEmail());

            return ResponseEntity.ok(Map.of("message", "Neuer Verifizierungscode wurde gesendet"));

        } catch (Exception ex) {
            logger.error("Failed to resend verification email", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Fehler beim Senden des Codes: " + ex.getMessage()));
        }
    }

    // Private Helper Methods

    /**
     * Generate 6-digit verification code
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}