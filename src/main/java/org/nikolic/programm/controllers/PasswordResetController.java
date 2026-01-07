package org.nikolic.programm.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.nikolic.programm.dtos.ApiResponse;
import org.nikolic.programm.dtos.PasswordResetRequest;
import org.nikolic.programm.entities.PasswordReset;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.exceptions.ValidationException;
import org.nikolic.programm.repositories.PasswordResetRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.EmailService;
import org.nikolic.programm.services.PasswordResetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * PasswordResetController - Mit PasswordReset Entity
 *
 * Änderungen:
 * - Verwendet PasswordReset Entity statt Transient Felder
 * - Bessere Security (IP-Tracking, Attempt-Limiting)
 * - Cleanup alter Resets
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * Request password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String email = request.get("email");
        String ipAddress = getClientIP(httpRequest);

        PasswordResetService.PasswordResetResult result =
                passwordResetService.initiateReset(email, ipAddress);

        if (!result.isSuccess()) {
            throw new ValidationException(result.getMessage());
        }

        return ResponseEntity.ok(
                ApiResponse.success(result.getMessage())
        );
    }

    /**
     * Verify reset code
     */
    @PostMapping("/verify-reset-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyResetCode(
            @Valid @RequestBody Map<String, String> request) {

        String email = request.get("email");
        String code = request.get("code");

        PasswordResetService.PasswordResetResult result =
                passwordResetService.verifyCode(email, code);

        if (!result.isSuccess()) {
            throw new ValidationException(result.getMessage());
        }

        Map<String, String> data = Map.of(
                "resetToken", result.getToken()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Code verifiziert", data)
        );
    }

    /**
     * Reset password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        PasswordResetService.PasswordResetResult result =
                passwordResetService.resetPassword(
                        request.getEmail(),
                        request.getCode(),
                        request.getNewPassword()
                );

        if (!result.isSuccess()) {
            throw new ValidationException(result.getMessage());
        }

        return ResponseEntity.ok(
                ApiResponse.success("Passwort erfolgreich geändert")
        );
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader == null ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }
}