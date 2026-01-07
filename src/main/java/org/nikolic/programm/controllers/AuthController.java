package org.nikolic.programm.controllers;

import jakarta.validation.Valid;
import org.nikolic.programm.dtos.ApiResponse;
import org.nikolic.programm.dtos.LoginRequest;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.exceptions.AccountInactiveException;
import org.nikolic.programm.exceptions.AuthenticationFailedException;
import org.nikolic.programm.exceptions.EmailNotVerifiedException;
import org.nikolic.programm.exceptions.InvalidCredentialsException;
import org.nikolic.programm.services.JwtService;
import org.nikolic.programm.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(
            UserService userService,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Login endpoint with custom exceptions
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        // Find user - throws UserNotFoundException if not found
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException());

        // Check if active
        if (!user.isActive()) {
            throw new AccountInactiveException();
        }

        // Check if verified
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        // Authenticate - throws BadCredentialsException if invalid
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate token
        String token = jwtService.generateToken(user);

        Map<String, Object> data = Map.of(
                "token", token,
                "email", user.getEmail(),
                "fullname", user.getFullname(),
                "id", user.getId(),
                "role", user.getRoleString()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Login erfolgreich", data)
        );
    }

    /**
     * Get current user
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            Authentication authentication) {

        User user = userService.getAuthenticatedUser(authentication)
                .orElseThrow(() -> new AuthenticationFailedException("Nicht angemeldet"));

        Map<String, Object> userData = Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullname", user.getFullname(),
                "role", user.getRoleString(),
                "isActive", user.isActive(),
                "createdAt", user.getCreatedAt()
        );

        return ResponseEntity.ok(ApiResponse.success(userData));
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(
                ApiResponse.success("Erfolgreich abgemeldet")
        );
    }
}
