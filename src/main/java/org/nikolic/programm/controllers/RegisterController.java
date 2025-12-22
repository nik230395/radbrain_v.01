package org.nikolic.programm.controllers;

import org.nikolic. programm. dtos.RegisterRequest;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.nikolic. programm.repositories.UserRepository;
import org. nikolic.programm.services.EmailService;
import org. nikolic.programm.services. JwtService;
import org.springframework.http.HttpStatus;
import org.springframework. http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java. util.Map;
import java. util.Random;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class RegisterController {

    private static final Logger logger = LoggerFactory. getLogger(RegisterController.class);

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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Enhanced Validation
            if (request.getEmail() == null || request.getPassword() == null || request.getFullname() == null) {
                return ResponseEntity. badRequest().body(Map.of("error", "Alle Felder sind erforderlich"));
            }

            if (request.getFullname().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Name darf nicht leer sein"));
            }

            if (request.getPassword().length() < 8) {
                return ResponseEntity.badRequest().body(Map.of("error", "Passwort muss mindestens 8 Zeichen haben"));
            }

            if (! isValidEmail(request.getEmail())) {
                return ResponseEntity. badRequest().body(Map.of("error", "Ungültige E-Mail-Adresse"));
            }

            // Check if user already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity. badRequest().body(Map.of("error", "E-Mail-Adresse bereits registriert"));
            }

            // Generate verification code
            String verificationCode = generateVerificationCode();
            logger.info("Generated verification code for {}: {}", request.getEmail(), verificationCode);

            // Create new user (inactive until verified)
            User user = new User();
            user.setEmail(request.getEmail().toLowerCase().trim());
            user.setFullname(request.getFullname().trim());
            user.setPassword_hash(passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.USER);
            user.setIs_active(false); // Not active until email verified
            user.setEmailVerified(false);
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10)); // 10 minutes validity
            user.setCreated_at(LocalDateTime.now());

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
                        "message", "Registrierung erfolgreich.  Bestätigungscode wurde per E-Mail gesendet.",
                        "requiresVerification", true,
                        "email", savedUser.getEmail()
                ));

            } catch (Exception emailError) {
                // If email sending fails, delete the user to keep database clean
                userRepository.delete(savedUser);
                logger.error("Failed to send verification email to:  {}, user deleted", savedUser.getEmail(), emailError);

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "E-Mail konnte nicht gesendet werden.  Bitte versuchen Sie es später erneut."));
            }

        } catch (Exception ex) {
            logger.error("Registration failed for email: {}", request.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registrierung fehlgeschlagen:  " + ex.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<? > verifyEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request. get("email");
            String code = request.get("code");

            if (email == null || code == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "E-Mail und Code sind erforderlich"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email. toLowerCase().trim());
            if (userOpt.isEmpty()) {
                logger.warn("Verification attempt for non-existent user: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Benutzer nicht gefunden"));
            }

            User user = userOpt.get();

            // Check if already verified
            if (user.isEmailVerified()) {
                logger.warn("Verification attempt for already verified user: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "E-Mail bereits verifiziert"));
            }

            // Check verification code
            if (!code.equals(user.getVerificationCode())) {
                logger. warn("Invalid verification code for user: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Ungültiger Verifizierungscode"));
            }

            // Check if code is expired
            if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
                logger.warn("Expired verification code for user: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Verifizierungscode ist abgelaufen"));
            }

            // Activate user
            user.setEmailVerified(true);
            user.setIs_active(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);

            logger.info("User successfully verified and activated: {}", user.getEmail());

            // Send welcome email (non-blocking)
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getFullname());
            } catch (Exception e) {
                logger.warn("Failed to send welcome email to:  {}", user.getEmail(), e);
                // Don't fail the verification process if welcome email fails
            }

            // Generate JWT Token
            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "message", "E-Mail erfolgreich verifiziert",
                    "token", token,
                    "email", user.getEmail(),
                    "fullname", user.getFullname(),
                    "roles", new String[]{"USER"}
            ));

        } catch (Exception ex) {
            logger.error("Email verification failed", ex);
            return ResponseEntity. status(HttpStatus.INTERNAL_SERVER_ERROR)
                    . body(Map.of("error", "Verifizierung fehlgeschlagen:  " + ex.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "E-Mail ist erforderlich"));
            }

            Optional<User> userOpt = userRepository. findByEmail(email.toLowerCase().trim());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Benutzer nicht gefunden"));
            }

            User user = userOpt.get();

            if (user.isEmailVerified()) {
                return ResponseEntity.badRequest().body(Map.of("error", "E-Mail bereits verifiziert"));
            }

            // Generate new verification code
            String newCode = generateVerificationCode();
            user.setVerificationCode(newCode);
            user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);

            logger.info("Generated new verification code for {}: {}", email, newCode);

            // Send new verification email
            emailService.sendVerificationEmail(user.getEmail(), user.getFullname(), newCode);

            logger.info("Resent verification email to: {}", user.getEmail());

            return ResponseEntity.ok(Map.of("message", "Neuer Verifizierungscode wurde gesendet"));

        } catch (Exception ex) {
            logger. error("Failed to resend verification email", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Fehler beim Senden des Codes:  " + ex.getMessage()));
        }
    }

    // Helper Methods
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(. +)$") && email.contains(".");
    }
}