package org.nikolic.programm.controllers;

import jakarta.validation.Valid;
import org.nikolic.programm.dtos.ApiResponse;
import org.nikolic.programm.dtos.RegisterRequest;
import org.nikolic.programm.dtos.VerifyEmailRequest;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.exceptions.DuplicateEmailException;
import org.nikolic.programm.exceptions.ValidationException;
import org.nikolic.programm.services.JwtService;
import org.nikolic.programm.services.RegistrationCacheService;
import org.nikolic.programm.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Registration Controller
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class RegisterController {

    private final RegistrationCacheService registrationService;
    private final UserService userService;
    private final JwtService jwtService;

    public RegisterController(
            RegistrationCacheService registrationService,
            UserService userService,
            JwtService jwtService) {
        this.registrationService = registrationService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody RegisterRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        // Check if user exists
        if (userService.emailExists(email)) {
            throw new DuplicateEmailException(email);
        }

        // Create registration and send verification email
        registrationService.createRegistrationAndSendCode(
                request.getFullname(),
                email,
                request.getPassword()
        );

        Map<String, Object> data = Map.of(
                "requiresVerification", true,
                "email", email
        );

        return ResponseEntity.ok(
                ApiResponse.success("Registrierung erfolgreich. Bestätigungscode wurde gesendet.", data)
        );
    }

    /**
     * Verify email
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        User user = registrationService.verifyAndCreateUser(
                request.getEmail(),
                request.getCode()
        );

        if (user == null) {
            throw new ValidationException("Ungültiger oder abgelaufener Code");
        }

        // Generate token
        String token = jwtService.generateToken(user);

        Map<String, Object> data = Map.of(
                "token", token,
                "email", user.getEmail(),
                "fullname", user.getFullname(),
                "role", user.getRoleString()
        );

        return ResponseEntity.ok(
                ApiResponse.success("E-Mail erfolgreich verifiziert", data)
        );
    }

    /**
     * Resend verification code
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("E-Mail ist erforderlich");
        }

        registrationService.resendCode(email);

        return ResponseEntity.ok(
                ApiResponse.success("Neuer Verifizierungscode wurde gesendet")
        );
    }
}
